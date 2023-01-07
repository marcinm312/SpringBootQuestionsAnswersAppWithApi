# Questions Answers Application

This application is a bit similar to the discussion forum. After registration of the user in the application and logging in, the user can ask a new question to all website users or browse the existing questions of other users. Then, users can answer existing questions. Questions and answers can be exported to PDF or Excel files.

## Functions:
1. Registration and activation of the user via the link in the received e-mail;
2. Viewing questions and answers - the ability to search, sort entries by various attributes, pagination;
3. Adding questions and answers;
4. It is possible to modify and delete only your own entries! The exception is a user with administrator permissions who can modify and delete any entries;
5. The user, who asked the question, receives an e-mail notification when another user adds or updates the answer;
6. Exporting questions or answers to Excel or PDF;
7. Managing your user profile - reviewing your data, changing your data or password, the ability to log out of all other devices, unregister from the portal (this operation also deletes all user entries).

## Used technologies and libraries:
1. Java
2. Maven
3. Spring Boot
4. Spring Data, JPA, Hibernate
5. Flyway
6. PostgreSQL
7. Spring Security
8. Apache POI
9. JasperReports
10. Lombok
11. Spring Boot Starter Test, JUnit, Mockito, MockMvc
12. Swagger
13. HTML, JSP, CSS
14. Bootstrap

## Environment variables that need to be set:
1. **ACTIVATE_USER_URL** - Sets a link to activate the user account in the email. You can set an empty value ("" or " " - without quotes) when using the default built-in application frontend. If the link in the email should redirect to another page, enter its address, e.g. `http://localhost:3000/api/token?value=`. The value of the token (in this case, the value of the "value" parameter) will be added by the application to the end of the URL.
2. **ADMIN_DEFAULT_EMAIL** - The e-mail address that will be set to the first user (administrator) when the application is launched for the first time. Value example: `abcd@example.com`.
3. **ADMIN_DEFAULT_PASSWORD** - The password that will be set for the first user (administrator) when the application is started for the first time. After the first login as administrator, it is recommended to change its password in the application, leaving the old value of this environment variable. Value example: `password`.
4. **JDBC_DATABASE_URL** - URL to the database. Value example: `jdbc:postgresql://localhost:5432/database_name`.
5. **JDBC_DATABASE_USERNAME** - Database username. Value example: `postgres`.
6. **JDBC_DATABASE_PASSWORD** - Database user password. Value example: `yourPassword`.
7. **JWT_SECRET** - The password needed to hash the JWT signature. It should be remembered that the secret should be long and consist of different characters, because by breaking the secret, you can impersonate the authorization service and enter your data in payload.
8. **JWT_EXP_MINUTES** - The time, expressed in minutes, that determines how long the JWT token is valid. Value example: `60`.
9. **LOGGING_FILE_NAME** - Log file path and name. Names can be an exact location (for instance, `C://logs/server.log`) or relative (for instance, `logs/server.log`) to the current directory (project root directory or directory containing packaged war/jar file). You can set an empty value ("" or " " - without quotes) when using only console logs (without saving logs to a file).
10. **MAIL_HOST** - SMTP server host. Value example: `smtp.office365.com`.
11. **MAIL_PORT** - SMTP server port. Value example: `587`.
12. **MAIL_USERNAME** - The username (login) of the mail server. Value example: `example.user@abcde.com`.
13. **MAIL_PASSWORD** - Mail server user password. Value example: `yourPassword`.
14. **TIME_ZONE** - Time zone, for instance `Europe/Warsaw`.

## Steps to Setup

#### 1. Clone the repository

```bash
git clone https://github.com/marcinm312/SpringBootQuestionsAnswersAppWithApi.git
```

#### 2. Configure PostgreSQL

First, create a database with any name, e.g. `questions_answers_app`. You will use this name when setting the `JDBC_DATABASE_URL` environment variable. If you name the database as in the previous example, you should set the `JDBC_DATABASE_URL` environment variable as `jdbc:postgresql://localhost:5432/questions_answers_app`.

### Option 1

#### 3. Create a launch configuration in your favorite IDE

Using the example of IntelliJ IDE, select **JDK (Java) version 17**. Select the main class: `pl.marcinm312.springquestionsanswers.SpringBootQuestionsAnswersApplication` and set the environment variables as described above.

#### 4. Run the application using the configuration created in the previous step.

### Option 2

#### 3. Configure the environment variables on your OS as described above

#### 4. Package the application and then run it like so

Type the following commands from the root directory of the project:
```bash
mvn clean package
java -Dfile.encoding=UTF-8 -jar spring-boot-questions-answers-0.0.1-SNAPSHOT.war
```

## API documentation
After launching the application, the API documentation is available at the URL:
http://localhost:8080/swagger-ui/
