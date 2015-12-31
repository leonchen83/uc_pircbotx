FROM java:openjdk-7

RUN adduser --disabled-password --gecos "" uc_pircbotx
WORKDIR /home/uc_pircbotx/

# drop root privs
USER uc_pircbotx

# copy the bot into the user's home dir
ADD target/uc_pircbotx-0.3-SNAPSHOT-jar-with-dependencies.jar .
ADD src/main/resources/uc_pircbotx.json.example uc_pircbotx.json
CMD ["java", "-jar", "uc_pircbotx-0.3-SNAPSHOT-jar-with-dependencies.jar"]
