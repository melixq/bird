## xTweet - Backend for UrFU "Bird" project

### Build & Run

To build the project, follow the steps below.
1. Install prerequisites
  - JDK 17 with `JAVA_HOME` pointing to the right version
  - Gradle 8.7
  - Docker

2. Clone the repository from GitHub
    ```shell
    cd $HOME
    git clone -b feat/github-oauth https://github.com/melixq/bird.git
    ```
2. Go into the folder
    ```shell
    cd $HOME/bird
    ```

3. Set up the database by running the provided SQL script.
    ```shell
    docker run -e MYSQL_ROOT_PASSWORD=passw -d --name bird -v bird-db-data:/var/folders/mysql/data -v ./database:/database -p 3306:3306 mysql:latest
    ```

4. Create databases for MySQL and then tables. After that import data there.
    ```shell
    docker exec -it bird mysql -u root -ppassw -e "create database ums; create database twitter;"
    docker exec -it bird mysql -u root -ppassw -e "use ums; source /database/ums.sql"
    docker exec -it bird mysql -u root -ppassw -e "use twitter; source /database/twitter.sql"
    ```

5. Build your Java applications using Gradle
    - UMS service
        ```shell
        cd $HOME/bird/ums
        gradle build
        ```
    - Messaging service
        ```shell
        cd $HOME/bird/twitter
        gradle build
        ```

6. Start the application and check how it works
    - UMS service
        ```shell
        cd $HOME/bird/ums/build/libs/
        java -jar ums-1.2.jar
        ```
    - Messaging service
        ```shell
        cd $HOME/bird/ums/build/libs/
        java -jar twitter-1.2.jar
        ```

### Verify How It Works
As a result you should have 2 separate services running on your local machine using ports `9000` and `9001` accordingly. Import Bruno collections from the `requests` folder into your Bruno client to check how it works.

## License 
This repository is licensed under the [BSD 2-Clause License](LICENSE).
