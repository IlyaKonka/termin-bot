#!/bin/bash

# This scrip was created to increase bot stability on heroku.
# Heroku provides only 500MB RAM (after usage of 1GB shuts the container down).
# The script restarts java program every hour to clean all possible cache.

echo "FIRST START..."
java -jar chrome-bot-1.0-jar-with-dependencies.jar global.properties &

for (( ; ; ))
do
   sleep 3600
   pkill java && java -jar chrome-bot-1.0-jar-with-dependencies.jar global.properties &
done