# Executor

Projekt startowy do 90-minutowego ćwiczenia z teorii śladów. Program czyta dane
ze standardowego wejścia i wypisuje alfabet oraz pozycje wystąpień w słowie.

## Budowanie i uruchamianie

Użyj **JDK 26** i Mavena **3.9 lub nowszego**. Sprawdź w `mvn -version`, czy
Maven korzysta z JDK 26. Runner korzysta z wątków wirtualnych i podglądowego
API `StructuredTaskScope`. Maven włącza preview przy kompilacji i testach.

```bash
mvn clean verify
java --enable-preview -jar target/executor-1.0-SNAPSHOT.jar < src/test/resources/cw5-zadanie1.txt
```

## Wejście

Pierwsze zadanie z CW5 w formacie tekstowym UTF-8.

```text
A = {a, b, c, d}
a: x := x + y
b: y := y + 2z
c: x := 3x + z
d: z := y - z
w = baadcb
```

- Pierwszy wiersz deklaruje alfabet. Każdy symbol to jedna litera `A–Z` lub
  `a–z`. Definicje transakcji mają po jednym wierszu w dowolnej kolejności
  między alfabetem a słowem. Każdy symbol ma dokładnie jedną definicję.
- Przypisanie zapisuje do jednej zmiennej. Nazwy zmiennych zaczynają się od
  litery ASCII i mogą zawierać cyfry oraz `_`. Prawa strona jest sumą lub
  różnicą liczb całkowitych i zmiennych z ewentualnymi współczynnikami,
  np. `3x`. Można użyć `:=`, `←`, `<-` lub `=` jako znaku przypisania.
- Wiersz `w = ...` albo `u = ...` zawiera słowo bez odstępów wewnątrz. Samo
  `w =` oznacza słowo puste. Pusty alfabet zapisuje się jako `A = {}`.
  Puste wiersze są pomijane.

## Gotowe elementy i TODO

- `InputParser` waliduje wejście. `TraceInput` zachowuje kolejność deklaracji
  alfabetu i wszystkie wystąpienia w słowie. `Transaction.reads()` zwraca
  zmienne z prawej strony, a `writes()` zwraca zmienną docelową. Są to zbiory
  odczytu i zapisu używane w aktualnych materiałach CW10 i Z06.
- `TransactionOperation` wykonuje przypisanie na współdzielonych liczbach
  całkowitych. Każda zmienna ma na początku wartość `0`. `AbstractOperation`
  dodaje opóźnienie 10–19 ms i drukuje po sukcesie oznaczenie takie jak
  `a[2]`, gdzie liczba to pozycja w słowie liczona od 1.
- `ConcurrentBlockRunner` zgłasza cały blok do `StructuredTaskScope`, zwalnia
  bramkę startową i czeka na zakończenie. Błąd lub przerwanie zatrzymuje blok
  i jest przekazywane do wywołującego po zamknięciu zakresu.
- W `Executor` uzupełnij `foataClasses` i `runClasses`. Pierwsza metoda
  wyznacza klasy w kolejności wykonania. Dane transakcji są dostępne przez
  `operation.occurrence().transaction()`. Druga metoda przekazuje klasy kolejno
  do `BlockRunner.runBlock`. Klasa może ruszyć po zakończeniu poprzedniej.

Podział klas i mechanizm runnera pochodzą z [1DMeshParallel](https://github.com/macwozni/1DMeshParallel/tree/359d963a935e97240d1454c30870dd8c47fffaaf).
