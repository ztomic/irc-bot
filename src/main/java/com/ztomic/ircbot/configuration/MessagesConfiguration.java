package com.ztomic.ircbot.configuration;

import java.util.List;
import java.util.Random;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
@ConfigurationProperties(prefix = "msg", ignoreUnknownFields = false)
@Data
public class MessagesConfiguration {
	
	private static Random RANDOM = new Random();

	private List<QuizMessages> quiz;
	
	public QuizMessages getQuizMessages(String language) {
		return quiz.stream()
				.filter(m -> ObjectUtils.nullSafeEquals(language, m.getLanguage()))
				.findFirst()
				.orElseGet(QuizMessages::new);
	}
	
	@Data
	public static class QuizMessages {
		
		private String language = "CROATIAN";
		
		private String[] greetBigPlayers = {
				"Ladies and gentlemen, The Master of the Quizz himself -->",
				"Ono sto je Luke Skywalker u svemiru, na kvizu je -->",
				"Tesko je naci boljeg ili bolju nego sto je -->",
				"Smrtnici poklonite se, eto nam besmrtNi(c)ka -->",
				"Svi ste mi dragi, ali najdraz[i|a] mi je -->",
				"Fanfare zasvirajte, prostrite crveni sag, jer eto nama nase[g] -->",
				"Pozdrav Herkulu medju kvizoznancima! Dobrodos[ao|la]"
		};
		private String[] greetNormalPlayers = {
				"Ah.. znao sam da nam je nesto nedostajalo, to je nas[a]",
				"Popijmo nesto za dobra stara vremena, dobri stari znance",
				"Zaboga, pa gdje si tako dugo",
				"Glagol nedostajati izgubio je znacenje cim je us[ao|la]",
				"Dobrodos[ao|a] natrag",
				"Bilo nam je pusto bez tebe",
				"Sve do prije sekundice, imenica ceznja bila je sinonim za",
				"Ohoho.. eto stize poznato nam lice po imenu",
				"Eto nam kamencica koji je nedostajao nasem mozaiku. Pozdrav"
		};
		private String[] greetNewbies = {
				"Novi dan, novo lice :) Nadam se da ces se ugodno osjecati na nasem kanalu",
				"Dobrodos/ao/la u potprostorcic virtualnog prostora gdje se trazi malo znanja. Ugodan boravak",
				"Eto nam fazana :) Zovite ga",
				"Oho! Netko nov nam kuca na vrata, a zove se",
				"Brucosi, stisnite se malo. Eto vam kolege po imenu",
				"Dobrodos[ao|la]! Zelim ti da nadjes maleni trunak opipljive srece u ovome malenome kutku virtualnog prostora",
				"Raskomoti se, opusti se, ali napregni vijugice",
				"Dobrodos/ao/la na kviz. U ovoj sobi trebaju ti samo dobre vijugice i brzi prstici"
		};
		
		private String[] answerComments = new String[] {
				"Moj naklon",
				"Ljubim rukice",
				"Svaka cast",
				"Ide to vama",
				"Dajte i drugima priliku",
				"Eh, da ste jos i lijepi kao sto ste pametni",
				"Izvrsno",
				"Fenomenalno",
				"Maestralno",
				"Genijalnooo",
				"Duboko, duboko, nema sto",
				"Hasta la vista baby",
				"Tako ste neusporedivi",
				"Bibliofilija je vasa bolest",
				"Mozak vam je otekao",
				"Fantazmagorichno",
				"Hallelujah",
				"Nevjerojatno",
				"Fan fan fantastichooo.."
		};
		
		private Formats formats = new Formats();
		
		public String getRandomGreetBigPlayer() {
			return greetBigPlayers[RANDOM.nextInt(greetBigPlayers.length)];
		}
		
		public String getRandomGreetNormalPlayer() {
			return greetNormalPlayers[RANDOM.nextInt(greetNormalPlayers.length)];
		}
		
		public String getRandomGreetNewbie() {
			return greetNewbies[RANDOM.nextInt(greetNewbies.length)];
		}
		
		public String getRandomAnswerComment() {
			return answerComments[RANDOM.nextInt(answerComments.length)];
		}
		
	}
	
}
