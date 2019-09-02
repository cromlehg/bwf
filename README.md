# BWF projects operator web-service
![BWF](logo.png "BWF")

### Требования
1. Ubuntu 18+
2. ps
3. useradd

### Описание

Осуществляет управление за проектами сделанынми на базе BWF кода. 
А также осущствляет мониторинг системы.

В системе есть:
1. Пользователи
2. Проекты
3. Сервисы
4. Базы данных

Пользователи могут иметь проекты.
Проекты содержат в себе сервисы.
Сервисы - это bwf based веб-сервисы.

Базы данных стоят отдельно.

Возможности:
1. Обновление проекта из репозитория (pull с ветки роботом, билд, распаковка, пернзапкск сервиса, если был запущен)
2. Запуск/остановка/перезапуск сервиса
3. Изменение конфига (с перезапуском сервиса, если был запущен)
4. Чтение логов
5. Удаление сервиса
6. Создание сервиса (указание паоаметров репы, базы данных - создастся по дефолту и зальеься)

БД
1. Создание пользователя
2. Создание базы
3. Просмотр баз
4. Просмотр таблиц в базах
5. Просмотр описания полей
6. Выполнение запроса

При создании проекта мастер предложит создать
базу автоматически.

### Технические заметки

BWF based проект в развернутом виде должен отвечать следующим требованиям

1. Иметь пользователя в системе <user>
2. Должен быть расположен в папке /home/<user>/bwf/env-<project-number>/ , где <project-number> - уникальный порядковый номер. Далее - это базовая папка среды

#### Струкутура базовой папки среды
Папка делится на prod рабочие версии и reserv - резервные копии. В каждой папке
могут находится несколько приложений (в составе проекта их может работать много). 
У каждого приложения есть репозиторий из которого происходит обновление, собраный рабочий проект,
а также папка с файлами, которыми оперирует проект. В папке резерва структура практически такая же.
За искобчением того что присутсвует время <datetime> формирования резерва и могут отсуствовать любые
части. 

Резевр баз данных имеет свои номер, посклольку в общем случае одному приложению могут соответсвовать 
несколько баз данных.

1. /prod/instance-<app-number>/repo - github репозиторий 
2. /prod/instance-<app-number>/build - собраный рабочий проект
3. /prod/instance-<app-number>/storage - хранение файлов проекта
4. /reserv/<datetime>/instance-<app-number>/repo - github репозиторий 
5. /reserv/<datetime>/instance-<app-number>/build - собраный рабочий проект
6. /reserv/<datetime>/instance-<app-number>/storage - хранение файлов проекта
7. /reserv/<datetime>/db-<db-number>/ - хранение файлов баз данных

##### Упрощения для рабочего прототипа
Один порект - одна база - один инстанс, проекты идут как env-(prject-number) и в них структура инстанса, а также все бэкапы

Первоочередные цели:
1. Создание 
2. Обновление

###### Создание
1. Создается структура папок
2. Качается репоизиторий
3. Создается база данных
4. Создаются таблицы
5. Инициализируются таблицы начальными данными
6. Создается application.conf из application.example.conf
7. Заполняются БД парамтеры и медиа
8. Создается дистрибутив
9. Распаковывается дистрибутив
10. Запускается 

#### Сущности системы:
1. Пользователь - это именно пользователь системы
2. Проект - на одного пользователя может быть несколько проектов
3. Приложение - bwf based запускаемый сервис в контексте одного проекта

##### Создание пользователя:

__Система должна работать от пользователя в группе sudoers__

Перед созданием пользователя система акутализирует информацию обо всех пользователях.
1. Чиатет пользователей системы
2. Сравинивает есть ли они в базе 
3. Если нет, то доблавляет с соответсвующим флагом. Если в базе есть пользователи, которых нет в системе, то помечает их как проблемные, если по ним есть проекты, если проектов нет, то удаляет наверное.

Создание пользователя происходит с помощью утилиты adduser. 

sudo useradd -m -p $(openssl passwd <password>) <username>  

Нужно найти способ шифровать на стороне бэкенда. 
А также способ исполнять комманды добавления пользователя без пароля судоера

Как происходит создание:
1. Вводим пароль от пользователя от которого работает operator
2. echo <operatoruserpassword> | sudo -S useradd -m -p $(openssl passwd <password>) <user>
3. Читаем пользователей системы, если появился, то обновляем базу


