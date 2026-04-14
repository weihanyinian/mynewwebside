import * as fs from 'fs';
import * as path from 'path';

const content = fs.readFileSync('e:/mywebside/每日一句.txt', 'utf-8');
const lines = content.split('\n');

const quotes = [];

for (let i = 0; i < lines.length; i++) {
  const line = lines[i].trim();
  if (!line) continue;

  const match = line.match(/^\d+\.\s*(.+)$/);
  if (match && !line.includes('英文名言')) {
    quotes.push(match[1].trim());
  } else if (!line.match(/^\d+\./) && !line.includes('---') && !line.includes('英文名言')) {
    // This is for the Chinese translations under English quotes
    if (quotes.length > 0 && !line.match(/^[a-zA-Z]/)) {
       quotes[quotes.length - 1] += ' - ' + line;
    }
  }
}

const outputPath = path.join('e:/mywebside/frontend/src/assets/quotes.json');
fs.writeFileSync(outputPath, JSON.stringify(quotes, null, 2), 'utf-8');
console.log(`Parsed ${quotes.length} quotes to ${outputPath}`);
