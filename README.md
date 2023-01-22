# **Стековый процессор**

- Абузов Ярослав Александрович P33302
- `forth | stack | harv | hw | tick | struct | stream | mem | prob1`

![GitLab CI](https://img.shields.io/badge/gitlab%20ci-%23181717.svg?style=for-the-badge&logo=gitlab&logoColor=white)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white)

[![pipeline status](https://gitlab.se.ifmo.ru/Zavar30/stack-processor/badges/master/pipeline.svg)](https://gitlab.se.ifmo.ru/Zavar30/stack-processor/-/commits/master)
[![coverage report](https://gitlab.se.ifmo.ru/Zavar30/stack-processor/badges/master/coverage.svg)](https://gitlab.se.ifmo.ru/Zavar30/stack-processor/-/commits/master)

## **Язык программирования**

Форма Бэкуса-Наура для языка Zorth (Forth + Zavar30):

```
<base> ::= <words> | <function> <space> <base>
<word> ::= <number> | <command>
<words> ::= <word> <space> <words> | <loop> <space> <words> | <word>
<number> ::= [0-9]+
<command> ::= "+" | "-" | "*" | "/" | INC | DEC | "AND" | "OR" | "NEG" | "XOR" | "NOT" | "=" 
                  | "!=" | ">" | "<" | "SWAP" | "DROP" | "DUP" | "OVER" | "!" | "@" | "." 
                  | "EMIT" | "IN" | <var> | <name> | ".\"" <space> <string> | <if>
<function> ::= ":" <space> <name> <space> <words> <space> ";"
<loop> ::= "do" <space> <words> <space> "loop"
<if> ::= "if" <space> <words> <space> "endif"
<var> ::= "variable" <space> <name>
<string> ::= ([A-z] | [0-9])+ "\""
<name> ::= ([A-Z] | [a-z])+
<space> ::= " " | "\n"
```

- Основное свойство языка - запись всех действий в обратной польской нотации (т.е. 2 + 3 будет выглядеть так: 2 3 +). Основной объект действия команд - стек
- Имеется поддержка математических операторов, логических операторов, операторов сравнения, переменных, циклов, условных операторов и функций
- Все слова языка должны быть разделены пробелом или переносом строки
- Поддерживаются комментарии в формате // comment
- Все функции объявляются в самом начале программы, затем идёт основная программа
- Поддерживаются вложенные циклы и вложенные условные операторы
- Системы типов нет. Все представлено в виде целых чисел. Все команды оперируют со стеком целых чисел
- Переменные имеют глобальную область видимости (после объявления), можно объявить в любом месте программы

### **Детали языка. Положить число на стек**
Для того, чтобы положить число на стек - необходимо просто написать его.

_Пример:_ `6`, теперь 6 лежит на верхушке.

### **Детали языка. Функции**
Для объявления функции необходимо использовать следующий синтаксис: `": <имя> <тело> ;"`. 

_Пример:_ `: sumn d @ n @ d @ / DUP 1 + * * 2 / ;`

Внутри одной функци объявить другую нельзя. Для вызова функции необходимо написать ей имя, например: `ADD sumn 2 3`.

### **Детали языка. Переменные**
Для объявления переменной необходимо использовать следующий синтаксис: `"variable <имя>"`.

_Пример:_ `variable a`

Для получения адреса переменной необходимо написать её имя, на верхушке стека будет адрес переменной.

Для получения значения необходимо использовать следующий синтаксис: `"<имя> @"`. @ - оператор fetch.

_Пример:_ `a @`, теперь значение переменной лежит на верхушке стека.

Для записи значения необходимо использовать следующий синтаксис: `"<значение> <имя> !"`. ! - оператор store. Т.е. значение берется из верхушки стека.

_Пример:_ `15 a !`, теперь 15 хранится в переменной `a`.

### **Детали языка. Циклы**
Цикл будет повторять команды заключенные между `do ... loop`, пока на верхушке стека не 0. Вложенные циклы разрешены.

_Пример:_ `do 2 3 ADD loop`

_Пример:_ `do 1 IN EMIT loop` - бесконечный цикл из программы cat.

### **Детали языка. Условные операторы**
Будут выполнены команды внутри `if ... endif`, если на верхушке стека не 0. Вложенные `if` разрешены.

_Пример:_ `if 4 1 AND if OR * endif = endif`

### **Детали языка. Описание команд**
| Команда | Стек | Описание |
| ------ | ------ | ------ | 
| + | N1 N2 → (N1 + N2) | Складывает два первых операнда и возвращает сумму на вершину |
| \- | N1 N2 → (N1 \- N2) | Вычитает N1 из N2 и возвращает разность на вершину | 
| \* | N1 N2 → (N1 \* N2) | Умножает два первых элемента и возвращает произведение на вершину |
| / | N1 N2 → (N1 / N2) | Делит N1 на N2 и возвращает целую часть на вершину |
| AND | N1 N2 → (N1 & N2) | Побитовое логическое умножение N1 и N2 |
| OR | N1 N2 → (N1 \| N2) | Побитовое логическое сложение N1 и N2 |
| NEG | N1 → -N1 | Отрицание N1 |
| XOR | N1 N2 → (N1 ^ N2) | «Cложение по модулю 2» N1 и N2 |
| NOT | N1 → !N1 | Побитовое логическое отрицание N1 |
| = | N1 N2 → (N1 == N2) | Проверка на равенство N1 и N2. Если равны, то результат 1, иначе 0 |
| != | N1 N2 → (N1 != N2) | Проверка на неравенство N1 и N2. Если неравны, то результат 1, иначе 0 |
| > | N1 N2 → (N2 > N1) | Проверка, что N2 больше N1. Если так, то результат 1, иначе 0 |
| < | N1 N2 → (N2 < N1) | Проверка, что N2 меньше N1. Если так, то результат 1, иначе 0 |
| INC | N1 → (N1 + 1) | Инкремент N1 |
| DEC | N1 → (N1 - 1) | Декремент N1 |
| SWAP | N1 N2 → N2 N1 | Меняет местами два верхних элемента стека |
| DROP | N1 N2 → N1 | Удаляет N1 из стека |
| DUP | N1 → N1 N1 | Копирует N1 на вершину |
| OVER | N1 N2 → N1 N2 N1 | N1 N2 → N1 N2 N1 |
| ! | N1 ADDR → | Сохраняет N1 по адресу ADDR в памяти данных |
| @ | ADDR → N1 | Считывает значение из памяти данных по адресу ADDR и возвращает его в N1 на вершину |
| . | N1 → | Выводит вершину стека в output |
| EMIT | N1 → | Выводит вершину стека в output как символ |
| IN | → N1 | Считывает значение из input |
