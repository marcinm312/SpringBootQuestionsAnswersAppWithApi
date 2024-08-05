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
13. Actuator
14. HTML, JSP, CSS
15. Bootstrap

## Environment variables that should be set:
|            Name            |                                                                                                                                                                           Description                                                                                                                                                                            |                  Example values                  | Default value |
|:--------------------------:|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|:------------------------------------------------:|:-------------:|
|  **ADMIN_DEFAULT_EMAIL**   |                                                                                                                        The e-mail address that will be set to the first user (admin) when the application is launched for the first time                                                                                                                         |                `abcd@example.com`                |               |
| **ADMIN_DEFAULT_PASSWORD** |                                                   The password that will be set for the first user (admin) when the application is started for the first time. After the first login as admin, it is recommended to change its password in the application, leaving the old value of this environment variable                                                   |                    `password`                    |               |
|   **JDBC_DATABASE_URL**    |                                                                                                                                                                       URL to the database                                                                                                                                                                        | `jdbc:postgresql://localhost:5432/database_name` |               |
| **JDBC_DATABASE_USERNAME** |                                                                                                                                                                        Database username                                                                                                                                                                         |                    `postgres`                    |               |
| **JDBC_DATABASE_PASSWORD** |                                                                                                                                                                      Database user password                                                                                                                                                                      |                  `yourPassword`                  |               |
|       **JWT_SECRET**       |                                                     The password needed to hash the JWT signature. It should be remembered that the secret should be long and consist of different characters, because by breaking the secret, you can impersonate the authorization service and enter your data in payload                                                      |                `1234password5678`                |               |
|    **JWT_EXP_MINUTES**     |                                                                                                                                         The time, expressed in minutes, that determines how long the JWT token is valid                                                                                                                                          |                       `30`                       |     `60`      |
|   **LOGGING_FILE_NAME**    | Log file path and name. Names can be an exact location (for instance, `C://logs/server.log`) or relative (for instance, `logs/server.log`) to the current directory (project root directory or directory containing packaged war/jar file). You can set an empty value ("" or " " - without quotes) when using only console logs (without saving logs to a file) |  `C://logs/server.log`, `logs/server.log`, ` `   |               |
|       **MAIL_HOST**        |                                                                                                                                                                         SMTP server host                                                                                                                                                                         |               `smtp.office365.com`               |               |
|       **MAIL_PORT**        |                                                                                                                                                                         SMTP server port                                                                                                                                                                         |                      `587`                       |               |
|     **MAIL_USERNAME**      |                                                                                                                                                             The username (login) of the mail server                                                                                                                                                              |             `example.user@abcde.com`             |               |
|     **MAIL_PASSWORD**      |                                                                                                                                                                    Mail server user password                                                                                                                                                                     |                  `yourPassword`                  |               |
|       **TIME_ZONE**        |                                                                                                                                                                            Time zone                                                                                                                                                                             |                 `Europe/Warsaw`                  |               |
|    **DATA_ROWS_LIMIT**     |                                                                                                                                                                 Maximum number of items per page                                                                                                                                                                 |                      `6000`                      |    `5000`     |

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
java -Dfile.encoding=UTF-8 -Djdk.util.jar.enableMultiRelease=false -jar target/spring-boot-questions-answers-0.0.1-SNAPSHOT.war
```

## API documentation
After launching the application, the API documentation is available at the URL:
http://localhost:8080/swagger-ui/index.html
