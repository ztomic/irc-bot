>
    ██╗██████╗  ██████╗      ██████╗  ██████╗ ████████╗
    ██║██╔══██╗██╔════╝      ██╔══██╗██╔═══██╗╚══██╔══╝
    ██║██████╔╝██║     █████╗██████╔╝██║   ██║   ██║   
    ██║██╔══██╗██║     ╚════╝██╔══██╗██║   ██║   ██║   
    ██║██║  ██║╚██████╗      ██████╔╝╚██████╔╝   ██║   
    ╚═╝╚═╝  ╚═╝ ╚═════╝      ╚═════╝  ╚═════╝    ╚═╝   

---
# [IRC](http://en.wikipedia.org/wiki/Internet_Relay_Chat) bot based on [PircBotX](http://code.google.com/p/pircbotx/), powered by [Spring Boot](http://projects.spring.io/spring-boot/)

## Features
* Quiz
	* questions with single or multiple correct answers stored in database (MySQL, PostgreSQL, ...) ([Spring Data 		JPA](http://projects.spring.io/spring-data-jpa/))
	* multilanguage questions (language per channel)
	* handling quiz games on multiple IRC channels and servers with single instance
	* challenges (duels between two players)
	* score statistics (current week, last week, current month, last month, total, fastest answer, ...)
	* currently quiz comments are mainly written in Croatian

Screenshot (using [KVIrc](http://www.kvirc.net/) as client):
![Screenshot.jpg](https://raw.githubusercontent.com/ztomic/irc-bot/master/irc-bot/doc/Screenshot.png)
