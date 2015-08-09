package com.ztomic.ircbot.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.ObjectUtils;

@ConfigurationProperties(prefix = "msg", ignoreUnknownFields = false)
public class MessagesConfiguration {
	
	private static Random RANDOM = new Random();

	private List<QuizMessages> quiz;
	
	public List<QuizMessages> getQuiz() {
		return quiz;
	}
	
	public void setQuiz(List<QuizMessages> quiz) {
		this.quiz = quiz;
	}
	
	public QuizMessages getQuizMessages(String language) {
		Optional<QuizMessages> messages = quiz.stream().filter(m -> ObjectUtils.nullSafeEquals(language, m.getLanguage())).findFirst();
		return messages.orElse(new QuizMessages());
	}
	
	public static class QuizMessages {
		
		private String language = "CROATIAN";
		
		private String[] greetBigPlayer = {
				"Ladies and gentlemen, The Master of the Quizz himself -->",
				"Ono sto je Luke Skywalker u svemiru, na kvizu je -->",
				"Tesko je naci boljeg ili bolju nego sto je -->",
				"Smrtnici poklonite se, eto nam besmrtNi(c)ka -->",
				"Svi ste mi dragi, ali najdraz[i|a] mi je -->",
				"Fanfare zasvirajte, prostrite crveni sag, jer eto nama nase[g] -->",
				"Pozdrav Herkulu medju kvizoznancima! Dobrodos[ao|la]"
		};
		private String[] greetNormalPlayer = {
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
		private String[] greetNewbie = {
				"Novi dan, novo lice :) Nadam se da ces se ugodno osjecati na nasem kanalu",
				"Dobrodos/ao/la u potprostorcic virtualnog prostora gdje se trazi malo znanja. Ugodan boravak",
				"Eto nam fazana :) Zovite ga",
				"Oho! Netko nov nam kuca na vrata, a zove se",
				"Brucosi, stisnite se malo. Eto vam kolege po imenu",
				"Dobrodos[ao|la]! Zelim ti da nadjes maleni trunak opipljive srece u ovome malenome kutku virtualnog prostora",
				"Raskomoti se, opusti se, ali napregni vijugice",
				"Dobrodos/ao/la na kviz. U ovoj sobi trebaju ti samo dobre vijugice i brzi prstici"
		};
		
		public String getLanguage() {
			return language;
		}
		
		public void setLanguage(String language) {
			this.language = language;
		}
		
		public String[] getGreetBigPlayer() {
			return greetBigPlayer;
		}
		
		public void setGreetBigPlayer(String[] greetBigPlayer) {
			this.greetBigPlayer = greetBigPlayer;
		}
		
		public String getRandomGreetBigPlayer() {
			return greetBigPlayer[RANDOM.nextInt(greetBigPlayer.length)];
		}
		
		public String[] getGreetNormalPlayer() {
			return greetNormalPlayer;
		}
		
		public String getRandomGreetNormalPlayer() {
			return greetNormalPlayer[RANDOM.nextInt(greetNormalPlayer.length)];
		}
		
		public void setGreetNormalPlayer(String[] greetNormalPlayer) {
			this.greetNormalPlayer = greetNormalPlayer;
		}
		
		public String[] getGreetNewbie() {
			return greetNewbie;
		}
		
		public void setGreetNewbie(String[] greetNewbie) {
			this.greetNewbie = greetNewbie;
		}
		
		public String getRandomGreetNewbie() {
			return greetNewbie[RANDOM.nextInt(greetNewbie.length)];
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("QuizMessages [language=");
			builder.append(language);
			builder.append(", greetBigPlayer=");
			builder.append(Arrays.toString(greetBigPlayer));
			builder.append(", greetNormalPlayer=");
			builder.append(Arrays.toString(greetNormalPlayer));
			builder.append(", greetNewbie=");
			builder.append(Arrays.toString(greetNewbie));
			builder.append("]");
			return builder.toString();
		}
		
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MessagesConfiguration [quiz=");
		builder.append(quiz);
		builder.append("]");
		return builder.toString();
	}
	
}
