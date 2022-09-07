# Termin-Bot
## Idea
Termin (eng. appointment) is essential in Germany, especially in Berlin.
Sometimes it is tough to get a termin in public institutions and
this bot is a way to automate termin check and
send [Telegram](https://en.wikipedia.org/wiki/Telegram_(software)) notification, if free
termin was found.
The code could be also considered as a template for subsequent development.
## Getting Started
The principle of bot operation is to refresh a webpage with needed appointments periodically
[**TERMIN_URL**] and to recognize the change on it. E.g., the page always contains 
the phrase "No free appointments" [**NO_TERMIN_FOUND_STRING**].
Then some free appointments are added to the system,
and the phrase "No free appointments" disappears. There is a new phrase 
(e.g., "List of free appointments" [**TERMIN_FOUND_STRING**]) instead of the old one.
Bot recognizes the change on the webpage and sends Telegram notifications about free appointments.


To first use the program, you must create a properties file for termin-bot.

There is an example: _src/main/resources/global.properties_.

Here is a list of the properties:

**BOT_NAME** - The name of the bot. Used only in the log output. 

**BOT_PORT** - The port which the chrome driver uses to check termin webpage.

**BOT_TOKEN** - Unique telegram bot token. You can get it after the creation of the telegram bot. 
Use [BotFather](https://telegram.me/BotFather) to create a bot. If you want to start the program without 
telegram notifications, please start with the default value from the example properties file.

**USERS** - List of usernames from Telegram who get access to the bot. It could be ignored,
if you test the program without Telegram notifications.

**CHAT_IDS** - Telegram chat id, which is generated after the user sends the first message to the bot. Leave this field empty. It will be filled in automatically.

**TERMIN_URL**  - The link to a website with appointments. This termin-bot was tested with the following website:
_https://otv.verwalt-berlin.de/ams/TerminBuchen_ 
If you use it for the same resource, paste the link with the needed Visa type.

**TERMIN_FOUND_STRING** - The webpage's phrase complies with free appointments.
An example is provided for otv.verwalt-berlin.de.

**NO_TERMIN_FOUND_STRING** - The webpage's phrase complies with the absence of free appointments.
An example is provided for otv.verwalt-berlin.de

**BUTTON_ID** - The button which should be clicked to get a page with appointments.
An example is provided for otv.verwalt-berlin.de

## How to use
### Startup
After editing the properties file you can build this java program with [Maven](https://maven.apache.org/) or use
released version.

**You need Java 11 to run it.** 

To start the bot, run **jar** file with a path to the properties file as an argument.
```
java -jar chrome-bot-1.0-jar-with-dependencies.jar <path_to_properties_file>
```

If you start the program without an argument, the default path to properties is _src/main/resources/global.properties_.

### Run with Docker
You could also run the bot in [Docker](https://www.docker.com/) container.
Go to the root folder of the project and run the following commands:

```
docker build ...
docker run ...
```

**Important:** 
- Before the building of docker image be sure that the _jar-with-dependencies_ is present after maven build in 
_target_ folder. If you have problems with the build, create _target_ folder in the project root directory and paste the released 
version of jar file.

- By default, for docker image is used the properties from _src/main/resources/global.properties_. If you want to change 
  it, edit _src/main/docker/Dockerfile_
  
### Deploy on Heroku
If you want to host this bot, you can use [Heroku](https://heroku.com).
It is easy to host Docker containers there. You need only _heroku.yml_.

Find it here: _src/main/docker/heroku.yml_

Information on how to deploy docker containers on Heroku:
_https://devcenter.heroku.com/articles/build-docker-images-heroku-yml_
