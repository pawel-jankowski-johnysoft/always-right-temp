curl -X POST --location 'http://localhost:8083/connectors?expand=status&expand=info' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--data '@./jdbcsink-connector-config.json'
