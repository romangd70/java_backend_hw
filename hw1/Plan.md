# План проверки

- [Микро-резюме в Markdown](Resume.md)
- [Микро-резюме в Wiki-разметке](Resume.wiki)
- [Mermaid-блок-схема](Flowchart.mmd)
- [Dockerfile для сборки и запуска Java-приложения](Dockerfile)
- [README с командами запуска](README.md)

## Команды

```bash
docker build -t seminar1 .
docker run --rm -e SECOND_NUMBER=32 seminar1 10
```

Ожидаемый вывод:

```text
First number: 10
Second number: 32
Your calculation: 42
```
