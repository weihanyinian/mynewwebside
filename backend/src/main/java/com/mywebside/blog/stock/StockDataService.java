package com.mywebside.blog.stock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Fetches real-time, intraday, K-line and capital-flow data from free public APIs.
 * A-shares: Sina (quote) + Tencent (intraday) + EastMoney (capital flow)
 * US/HK:   Yahoo Finance v8/chart
 */
@Service
public class StockDataService {
  private static final Logger log = LoggerFactory.getLogger(StockDataService.class);
  private final RestClient restClient;
  private final ObjectMapper om;

  public StockDataService(RestClient.Builder rb, ObjectMapper om) {
    this.restClient = rb.build();
    this.om = om;
  }

  // ---- real-time quote (Sina for A, Yahoo for US/HK) ----

  public Quote fetchQuote(String code) {
    if (isAStock(code)) return fetchSinaQuote(code);
    return fetchYahooQuote(toYahooSymbol(code));
  }

  public List<Quote> fetchQuotes(List<String> codes) {
    List<Quote> list = new ArrayList<>();
    for (String c : codes) list.add(fetchQuote(c));
    return list;
  }

  // ---- intraday / time-sharing (分时图) ----

  /** Returns minute-by-minute price + avg-price points for the current trading day. */
  public IntradayData getIntraday(String code) {
    if (isAStock(code)) return fetchTencentIntraday(code);
    return fetchYahooIntraday(toYahooSymbol(code));
  }

  // ---- K-line (日K/周K/月K) ----

  public KlineData getKline(String code, String period) {
    String interval = switch (period) {
      case "week" -> "1wk";
      case "month" -> "1mo";
      default -> "1d";
    };
    if (isAStock(code)) return fetchYahooKline(toYahooSymbol(code), interval);
    return fetchYahooKline(toYahooSymbol(code), interval);
  }

  // ---- capital flow (A-share only) ----

  public CapitalFlow getCapitalFlow(String code) {
    if (!isAStock(code)) return new CapitalFlow(null, null, null, null, "仅A股支持资金流向");
    return fetchEastMoneyFlow(code);
  }

  // ---- A-share helpers ----

  private Quote fetchSinaQuote(String code) {
    String sinaCode = code; // sh600519 / sz000001
    try {
      String body = restClient.get().uri("https://hq.sinajs.cn/list=" + sinaCode)
          .header("Referer", "https://finance.sina.com.cn").retrieve().body(String.class);
      if (body == null || body.isBlank()) return Quote.unavailable(code);
      int s = body.indexOf("\""), e = body.lastIndexOf("\"");
      if (s < 0 || e <= s) return Quote.unavailable(code);
      String[] f = body.substring(s + 1, e).split(",");
      if (f.length < 32) return Quote.unavailable(code);
      return new Quote(code, f[0],
          new BigDecimal(f[3]), // current price
          new BigDecimal(f[2]), // yesterday close
          new BigDecimal(f[4]), // open
          new BigDecimal(f[5]), // high
          new BigDecimal(f[6]), // low
          Long.parseLong(f[8]), // volume
          f[30], f[31]); // date, time
    } catch (Exception ex) { log.warn("Sina quote failed for {}", code, ex); return Quote.unavailable(code); }
  }

  private IntradayData fetchTencentIntraday(String code) {
    // Tencent minute API: http://ifzq.gtimg.cn/appstock/app/minute/query?_var=min_data&code=sh600519
    try {
      String body = restClient.get()
          .uri("http://ifzq.gtimg.cn/appstock/app/minute/query?code=" + code)
          .header("Referer", "https://gu.qq.com").retrieve().body(String.class);
      if (body == null || body.isBlank()) return IntradayData.empty();

      JsonNode root = om.readTree(body);
      JsonNode data = root.path("data");
      if (data.isMissingNode()) return IntradayData.empty();

      // minute data: data.{code}.data
      JsonNode minArr = data.path(code).path("data");
      if (!minArr.isArray() || minArr.size() == 0) return IntradayData.empty();

      BigDecimal preClose = new BigDecimal(root.path("data").path(code).path("pre").asText("0"));
      List<IntraPoint> points = new ArrayList<>();
      for (JsonNode p : minArr) {
        String tm = p.has("time") ? p.path("time").asText() : p.path("tm").asText();
        BigDecimal price = new BigDecimal(p.path("price").asText("0"));
        BigDecimal volume = new BigDecimal(p.path("volume").asText("0"));
        BigDecimal avg = new BigDecimal(p.path("avg_price").asText(p.path("price").asText("0")));
        points.add(new IntraPoint(tm, price, volume, avg));
      }
      return new IntradayData(preClose, points);
    } catch (Exception ex) { log.warn("Tencent intraday failed for {}", code, ex); return IntradayData.empty(); }
  }

  // ---- Yahoo helpers (US/HK and A-share K-line) ----

  private Quote fetchYahooQuote(String symbol) {
    try {
      JsonNode root = fetchYahooChart(symbol, "1d", "1m");
      JsonNode result = root.path("chart").path("result");
      if (!result.isArray() || result.size() == 0) return Quote.unavailable(symbol);
      JsonNode meta = result.get(0).path("meta");
      BigDecimal price = new BigDecimal(meta.path("regularMarketPrice").asText("0"));
      BigDecimal prev = new BigDecimal(meta.path("previousClose").asText("0"));
      return new Quote(symbol, meta.path("symbol").asText(),
          price, prev,
          new BigDecimal(meta.path("regularMarketOpen").asText("0")),
          new BigDecimal(meta.path("regularMarketDayHigh").asText("0")),
          new BigDecimal(meta.path("regularMarketDayLow").asText("0")),
          meta.path("regularMarketVolume").asLong(), "", "");
    } catch (Exception ex) { log.warn("Yahoo quote failed for {}", symbol, ex); return Quote.unavailable(symbol); }
  }

  private IntradayData fetchYahooIntraday(String symbol) {
    try {
      JsonNode root = fetchYahooChart(symbol, "1d", "5m");
      JsonNode result = root.path("chart").path("result");
      if (!result.isArray() || result.size() == 0) return IntradayData.empty();

      JsonNode meta = result.get(0).path("meta");
      JsonNode timestamps = result.get(0).path("timestamp");
      JsonNode quotes = result.get(0).path("indicators").path("quote").get(0);

      BigDecimal prevClose = new BigDecimal(meta.path("previousClose").asText("0"));
      if (!timestamps.isArray()) return IntradayData.empty();

      List<IntraPoint> points = new ArrayList<>();
      JsonNode opens = quotes.path("open");
      JsonNode closes = quotes.path("close");
      JsonNode highs = quotes.path("high");
      JsonNode lows = quotes.path("low");
      JsonNode volumes = quotes.path("volume");

      for (int i = 0; i < timestamps.size(); i++) {
        if (closes.get(i).isNull()) continue;
        long ts = timestamps.get(i).asLong();
        String time = String.format("%02d:%02d", (ts / 3600 + 8) % 24, (ts % 3600) / 60);
        BigDecimal price = new BigDecimal(closes.get(i).asText());
        BigDecimal vol = volumes.get(i).isNull() ? BigDecimal.ZERO : new BigDecimal(volumes.get(i).asText());
        points.add(new IntraPoint(time, price, vol, price));
      }
      return new IntradayData(prevClose, points);
    } catch (Exception ex) { log.warn("Yahoo intraday failed for {}", symbol, ex); return IntradayData.empty(); }
  }

  private KlineData fetchYahooKline(String symbol, String interval) {
    String range = switch (interval) {
      case "1wk" -> "6mo";
      case "1mo" -> "2y";
      default -> "3mo";
    };
    try {
      JsonNode root = fetchYahooChart(symbol, range, interval);
      JsonNode result = root.path("chart").path("result");
      if (!result.isArray() || result.size() == 0) return KlineData.empty();

      JsonNode timestamps = result.get(0).path("timestamp");
      JsonNode quotes = result.get(0).path("indicators").path("quote").get(0);
      if (!timestamps.isArray()) return KlineData.empty();

      List<KlinePoint> points = new ArrayList<>();
      JsonNode opens = quotes.path("open"), closes = quotes.path("close");
      JsonNode highs = quotes.path("high"), lows = quotes.path("low"), vols = quotes.path("volume");

      for (int i = 0; i < timestamps.size(); i++) {
        if (opens.get(i).isNull() || closes.get(i).isNull()) continue;
        long ts = timestamps.get(i).asLong() * 1000L;
        points.add(new KlinePoint(
            ts,
            new BigDecimal(opens.get(i).asText()),
            new BigDecimal(closes.get(i).asText()),
            new BigDecimal(highs.get(i).asText()),
            new BigDecimal(lows.get(i).asText()),
            new BigDecimal(vols.get(i).isNull() ? "0" : vols.get(i).asText())
        ));
      }
      return new KlineData(symbol, points);
    } catch (Exception ex) { log.warn("Yahoo kline failed for {}", symbol, ex); return KlineData.empty(); }
  }

  private JsonNode fetchYahooChart(String symbol, String range, String interval) {
    try {
      String url = String.format(
          "https://query1.finance.yahoo.com/v8/finance/chart/%s?range=%s&interval=%s",
          symbol, range, interval);
      String body = restClient.get().uri(url).header("User-Agent", "Mozilla/5.0").retrieve().body(String.class);
      return om.readTree(body == null ? "{}" : body);
    } catch (Exception e) { return om.createObjectNode(); }
  }

  // ---- EastMoney capital flow ----

  private CapitalFlow fetchEastMoneyFlow(String code) {
    String secid = code.startsWith("sh") ? "1." + code.substring(2) : "0." + code.substring(2);
    try {
      String url = "https://push2.eastmoney.com/api/qt/stock/get?secid=" + secid
          + "&fields=f62,f64,f66,f72,f78,f184,f66";
      String body = restClient.get().uri(url)
          .header("Referer", "https://quote.eastmoney.com").retrieve().body(String.class);
      JsonNode root = om.readTree(body).path("data");
      if (root.isMissingNode()) return CapitalFlow.unavailable();
      // f62=主力净流入, f64=超大单流入, f66=大单流入, f72=中单流入, f78=小单流入, f184=散户流入
      BigDecimal majorIn = bd(root.path("f64"), null);
      BigDecimal largeIn = bd(root.path("f66"), null);
      BigDecimal mediumIn = bd(root.path("f72"), null);
      BigDecimal retailIn = bd(root.path("f78"), null);
      if (majorIn == null && retailIn == null) return CapitalFlow.unavailable();
      BigDecimal bigTotal = (majorIn != null && largeIn != null) ? majorIn.add(largeIn) : majorIn;
      return new CapitalFlow(bigTotal, mediumIn, retailIn, null, null);
    } catch (Exception ex) { log.warn("EastMoney flow failed for {}", code); return CapitalFlow.unavailable(); }
  }

  // ---- symbol helpers ----

  static boolean isAStock(String code) {
    return code.startsWith("sh") || code.startsWith("sz");
  }

  static String toYahooSymbol(String code) {
    if (code.startsWith("sh")) return code.substring(2) + ".SS";
    if (code.startsWith("sz")) return code.substring(2) + ".SZ";
    return code; // US stock ticker or HK like 0700.HK
  }

  private static BigDecimal bd(JsonNode n, BigDecimal def) {
    if (n == null || n.isNull() || n.isMissingNode()) return def;
    try { return new BigDecimal(n.asText()); } catch (Exception e) { return def; }
  }

  // ---- DTOs ----

  public record Quote(String code, String name, BigDecimal price, BigDecimal prevClose,
      BigDecimal open, BigDecimal high, BigDecimal low, long volume, String date, String time) {
    public static Quote unavailable(String code) {
      return new Quote(code, "", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, "", "");
    }
    public BigDecimal change() { return price.subtract(prevClose); }
    public BigDecimal changePct() {
      return prevClose.compareTo(BigDecimal.ZERO) > 0
          ? change().divide(prevClose, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
          : BigDecimal.ZERO;
    }
  }

  public record IntraPoint(String time, BigDecimal price, BigDecimal volume, BigDecimal avgPrice) {}
  public record IntradayData(BigDecimal preClose, List<IntraPoint> points) {
    public static IntradayData empty() { return new IntradayData(BigDecimal.ZERO, List.of()); }
  }

  public record KlinePoint(long timestamp, BigDecimal open, BigDecimal close, BigDecimal high, BigDecimal low, BigDecimal volume) {}
  public record KlineData(String symbol, List<KlinePoint> points) {
    public static KlineData empty() { return new KlineData("", List.of()); }
  }

  public record CapitalFlow(BigDecimal superLarge, BigDecimal medium, BigDecimal retail, BigDecimal small, String note) {
    public static CapitalFlow unavailable() { return new CapitalFlow(null, null, null, null, "暂无资金流向数据"); }
  }
}
