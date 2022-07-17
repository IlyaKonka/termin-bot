FROM selenium/standalone-chrome:4.3.0-20220706


COPY chrome-bot-1.0-SNAPSHOT-jar-with-dependencies.jar chrome-bot-1.0-SNAPSHOT-jar-with-dependencies.jar
COPY global.properties global.properties
COPY loop.sh loop.sh

USER root
 
RUN chmod +x loop.sh

USER seluser

CMD pkill python3 ; pkill java ; pkill Xvfb ; ./loop.sh

