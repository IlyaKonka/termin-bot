#!/bin/bash

echo "FIRST START..."
java -cp chrome-bot-1.0-SNAPSHOT-jar-with-dependencies.jar telegram.TelegramBot $PORT &

for (( ; ; ))
do
   sleep 3600
   pkill java && java -cp chrome-bot-1.0-SNAPSHOT-jar-with-dependencies.jar telegram.TelegramBot $PORT &
done