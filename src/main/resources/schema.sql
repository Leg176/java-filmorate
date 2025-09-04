DROP TABLE IF EXISTS Films CASCADE;
DROP TABLE IF EXISTS Genres CASCADE;
DROP TABLE IF EXISTS FilmGenres CASCADE;
DROP TABLE IF EXISTS Users CASCADE;
DROP TABLE IF EXISTS Friends CASCADE;
DROP TABLE IF EXISTS Likes CASCADE;

CREATE TABLE IF NOT EXISTS Films (
     idFilm  BIGINT PRIMARY KEY AUTO_INCREMENT,
     nameFilm VARCHAR(100) NOT NULL,
     description VARCHAR(2000) NOT NULL,
     releaseDate DATE NOT NULL,
     duration INT NOT NULL CHECK (duration > 0),
     ratingMPA VARCHAR(50) CHECK (ratingMPA IN ('G', 'PG', 'PG_13', 'R', 'NC_17'))
     );

CREATE TABLE IF NOT EXISTS Genres (
     idGenre INT PRIMARY KEY AUTO_INCREMENT,
     name VARCHAR(100) NOT NULL
     );

CREATE TABLE IF NOT EXISTS FilmGenres (
     idFilm  BIGINT,
     idGenre INT,
     PRIMARY KEY (idFilm, idGenre),
     FOREIGN KEY (idFilm) REFERENCES Films(idFilm) ON DELETE CASCADE,
     FOREIGN Key (idGenre) REFERENCES Genres(idGenre) ON DELETE CASCADE
     );

CREATE TABLE IF NOT EXISTS Users (
     idUser BIGINT PRIMARY KEY AUTO_INCREMENT,
     email VARCHAR(255) NOT NULL UNIQUE,
     login VARCHAR(100) NOT NULL,
     name VARCHAR(100),
     birthday DATE NOT NULL
     );

CREATE TABLE IF NOT EXISTS Likes (
     idFilm  BIGINT,
     idUser BIGINT,
     PRIMARY KEY (idFilm, idUser),
     FOREIGN KEY (idFilm) REFERENCES Films(idFilm) ON DELETE CASCADE,
     FOREIGN Key (idUser) REFERENCES Users(idUser) ON DELETE CASCADE
     );

 CREATE TABLE IF NOT EXISTS Friends (
      idUser BIGINT,
      idUserFriends BIGINT,
      PRIMARY KEY (idUser, idUserFriends),
      FOREIGN KEY (idUser) REFERENCES Users(idUser) ON DELETE CASCADE,
      FOREIGN KEY (idUserFriends) REFERENCES Users(idUser) ON DELETE CASCADE
      );