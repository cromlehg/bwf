[![codecov](https://codecov.io/gh/cromlehg/bwf/branch/master/graph/badge.svg?token=lrNcvuSIsO)](https://codecov.io/gh/cromlehg/bwf)

# BWF - BlockWit Web Framework
![BWF](logo.png "BWF")

### Roadmap 0.2
1. ~~Реализовать систему разграничения доступа с ролями и разрешенями (см. СРД)~~
2. ~~Перейти с ролей на разрешения~~
3. ~~Восстановление пароля~~
4. ~~Проверка валидности почты~~
5. ~~Debug фичи~~
6. Activity???
7. ~~RETURN URL~~
8. Добавление, редактирование и просмотр тэгов у статьи
9. ~~Меню ЛК на панели~~
10. ~~Список разрешений в административной панели~~
11. ~~Список ролей в административной панели~~
12. ~~Возможность создать роль в административной панели~~
13. Возможность удалить роль с административной панели (при этом связи удаляются у всех сущностей с предупреждением)
14. Просмотр списка разрешений (с обозначением роли если входит в роль) у пользователя из административной панели
15. Просмотр списка ролей у пользователя из административной панели
16. Возможность добавлять роль для пользователя в административной панели
17. Возможность удалять роль для пользователя в административной панели
18. Возможность удалять разрешения вне роли в административной панели для пользователя
19. Возможность добавлять разрешения вне роли в административной панели для пользователя
21. ~~Возможность создавать разрешение~~
22. ~~Просмотр списка разрешений у роли~~
23. ~~Редактирование параметров разрешения~~
24. ~~Редактирование параметров роли~~
25. Редактирование опций
26. Проектировние системы меню
27. Улучшения редактора (переключение между HTML и WYSIWYG, загрузка меди по умолчанию)
28. Безопасность загрузки JS внутри статьей
29. Разделение постов на типы системно (не давать доступ к контенту представленому в виде поста, если это страница а не пост)
30. Система шорткодов 
31. Шорткод отображения github кода
32. Система комментариев
33. Чат
34. Динамическое назначение ролей???
35. BlockChain - ETH ERC20 оплата???
36. Шорткод отображения других постов
37. ~~Список статей по тэгам~~
38. Подсветка синтаксиса для программного кода (blockwit)
39. Профиль c возможностью редактирования
40. Поиск статей по контенту
41. Фильтрация статей в админке
42. Больше информации с авторизации: IP, Date, Time, Device, OS, Browser, Location


Приоритетные фичи для формирования новой версии: 
~~2~~, 8, 30, 32, 36, ~~37~~, 38, 39, 28

Плановый срок Beta: 1.01.2019

Плановый срок релиза: 15.02.2019

### Система шорткодов
Во весрии 0.2 в качестве первой реализации используем шорткоды, в которых можно указывать страницу.
Шорткод имеет синатксис @{name params}. Где __name__ - имя обработчика шорткода a-z и символ "_" может быть. 
Для экранирования шорткода в HTML достаточно указать двойную скобку "[[".
 
#### Механизм обработки шорткодов
Любоей контент, который может иметь шортокоды будет обрабатываться следующим образом:
1. В контенте ищутся все шорткоды.
3. Выбираются все обработчики и группируются. Каждому обработчику в соответсвие ставится список точек с шорткодами. 
4. Далее каждому обработчику на вход подается список точек с шорткодам.
5. Обраотчик возвращает спсиок соотевтсвий точек шорткодов результату их обработки. Т.е. Sequence(shortcode, Option\[shortcodeProcessResult\]) = shortcodeProcessor(shortcodes)
6. Если обработчик не смог обработать параметры или такого обработчика нет, то шорткод в генерируемой странице просто вырезается.

##### Обработчик шорткода post
Данный обработчик в версии 0.2 будет иметь один параметр - id. Id - это идентификатор поста.
По идентификатору будет доставаться возвращаться контент поста с идентификатором id. 
В общем случае обработчик поста может зациклиться если будет обрабаваться все посты. Поэтому будет стоять ограничение на глубину обработки и на ширину обработки.
Эти ограничения будут находиться в опциях.    

### Система разраничениия доступа СРД
СРД состоит из двух сущностей:
1. Разрешения - маркер доступа к операции (порсмотр, удаление, создание, изменение - CRUD) 
2. Роли - группа разрешений
Любой сущности могут назначаться как роли так и разрешения.

#### Модель в БД
БД будет состоять из четырех таблиц: 
1. Роли
2. Разрешения
3. Назначения ролей на сущности
4. Назначения разрешений на сущности
5. Назначение разрешений на роли

##### Роли - roles
1. id
2. name - названиие согласно нотации, короткий текст, не может быть null
3. description - описание, может быть null

##### Разрешения - permissions
1. id
2. value - названиие согласно нотации, короткий текст, не может быть null
3. description - описание, может быть null

##### Назначения ролей на сущности - roles_to_targets
1. role_id
2. target_type - энумератор обозначений сущностей, на которые могут назначаться роли (в общем случае операции могут выполнять не только аккаунты)
3. target_id

##### Назначения разрешений на сущности - permissions_to_targets
1. role_id
2. target_type - энумератор обозначений сущностей, на которые могут назначаться роли (в общем случае операции могут выполнять не только аккаунты, но и на роли)
3. target_id

#### Пример реализации системы СРД для блоговой системы

##### Разрешения:
1. Создание статьи с уловиями - posts.create.conditional
2. Создание статьи без улсовий - posts.create.anytime
3. Безусловное редактирование своей статьи - posts.own.edit.anytime
4. Безусловное редактирование любой статьи - posts.any.edit.anytime
5. Условное редактирование своей статьи - posts.own.edit.conditional
6. Условное редактирование любой статьи - posts.any.edit.conditional
7. Удаление своей статьи - posts.own.remove
8. Удаление любой статьи - posts.any.remove
9. Просмотр открытых статей - posts.open.view
10. Просмотр любой статьи - posts.any.view
11. Просмотр своих списка статей - posts.own.list.view
12. Просмотр всех списка статей - posts.any.list.view
13. Просмотр опций - options.list.view
14. Просмотр списка пользователей accounts.list.view
15. Редактирование параметров аккаунтов - accounts.edit
16. Редактирование всех прав и ролей - permissions.any.edit
17. Редактирование опций - options.edit
18. Просмотр меню - menu.view
19. Права на все операции по всем комментариям - comments.any.edit -
20. Права на все операции по своим комментариям - comments.own.edit

##### Роли с разрешениями:
1. Администратор: 2, 4, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
2. Редактор: 1, 6, 9, 11, 12
3. Автор: 1, 5, 9, 11
4. Клиент: 9

### Классификация статей по тэгам
Тэгам можно назначать разрешения в таблице permissions_to_targets.
Если есть такое разрешение, то добавлять тэг к той или иной сущности может
только тот, кто имеет соответсвующее разрешение.

Назщвание у тэга может быть не уникальное. 
Например может быть тэг для того чтобы классифицировать статью как часть рубрики меню.
И тэг с таким же названием для общего пользования.  

#### Классы - тэги - tags
1. id
2. name - обязательное отображаемое имя
3. descr - опциональное описание
#### Назначение статьям классификтора (тэга) - tags_to_targets
1. tag_id
2. target_type
3. target_id


