FROM selenium/standalone-chrome:4.3.0-20220706


COPY target/chrome-bot-1.0-jar-with-dependencies.jar chrome-bot-1.0-jar-with-dependencies.jar
COPY src/main/resources/global.properties global.properties
COPY src/main/docker/loop.sh loop.sh

USER root
 
RUN chmod +x loop.sh

USER seluser

CMD pkill python3 ; pkill java ; pkill Xvfb ; ./loop.sh

