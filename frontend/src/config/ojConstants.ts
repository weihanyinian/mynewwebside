export const OJ_LANGUAGES = ['C', 'CPP', 'JAVA', 'PYTHON'] as const
export type OjLanguage = (typeof OJ_LANGUAGES)[number]

export interface LanguageOption {
  value: OjLanguage
  label: string
  monacoLang: string
}

export const ALL_LANGUAGE_OPTIONS: LanguageOption[] = [
  { value: 'C', label: 'C', monacoLang: 'c' },
  { value: 'CPP', label: 'C++', monacoLang: 'cpp' },
  { value: 'JAVA', label: 'Java', monacoLang: 'java' },
  { value: 'PYTHON', label: 'Python', monacoLang: 'python' },
]

export const CODE_TEMPLATES: Record<OjLanguage, string> = {
  C: `#include <stdio.h>

int main() {
    // 在这里编写你的代码

    return 0;
}`,
  CPP: `#include <iostream>
using namespace std;

int main() {
    // 在这里编写你的代码

    return 0;
}`,
  JAVA: `import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // 在这里编写你的代码

    }
}`,
  PYTHON: `# 在这里编写你的代码
def main():
    pass

if __name__ == "__main__":
    main()
`,
}

export type VerdictCode = 'AC' | 'WA' | 'TLE' | 'RE' | 'CE' | 'RUN_OK'

export const VERDICT_TEXT: Record<VerdictCode, string> = {
  AC: 'Accepted',
  WA: 'Wrong Answer',
  TLE: 'Time Limit Exceeded',
  RE: 'Runtime Error',
  CE: 'Compile Error',
  RUN_OK: 'Running',
}

export const VERDICT_CLASS: Record<VerdictCode, string> = {
  AC: 'verdict-ac',
  WA: 'verdict-wa',
  TLE: 'verdict-tle',
  RE: 'verdict-re',
  CE: 'verdict-ce',
  RUN_OK: 'verdict-running',
}

export function isValidLanguage(lang: string): lang is OjLanguage {
  return (OJ_LANGUAGES as readonly string[]).includes(lang)
}
