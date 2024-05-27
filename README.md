# Tema 2 - Planificarea de task-uri într-un datacenter

În cadrul acestei teme am implementat un sistem de planificare a unor task-uri, sub forma unui Dispatcher (Load Balancer) și a unor Noduri.

- **Dispatcher-ul** are rol de preluare a task-urilor ce sosesc și transmiterea lor mai departe către noduri pe baza unor politici de planificare.
- **Nodurile** primesc aceste task-uri pe care le rețin într-o coadă de priorități, iar apoi le execută.

## Implementarea Dispatcher-ului

În clasa `MyDispatcher` am implementat logica din spatele Dispatcher-ului, respectiv în metoda `addTask`. Metoda `addTask` este apelată de fiecare dată când un task nou intră în sistem. Astfel că, în cadrul acestei metode a fost necesară implementarea logicii politicii de planificare în funcție de care dispatcher-ul alege cărui nod să trimită task-ul.

### Politicile de planificare

#### Round Robin
Am folosit variabila `prev_id` pentru a reține id-ul următorului nod către care trebuie trimis task-ul conform formulei din enunț: `(prev_id + 1) % nr_noduri`. Astfel calculat, apelez metoda `addTask` a nodului cu id-ul corespunzător pentru a-l adăuga în coadă.

#### Shortest Queue
Pentru această politică, dispatcher-ul trimite task-ul către nodul care are coada cea mai mică. Pentru a face acest lucru, am iterat prin lista de hosts, iar pentru fiecare host am reținut size-ul cozii. Dacă dimensiunea cozii este mai mică decât cea găsită anterior, rețin host-ul. Altfel, dacă există două noduri cu aceeași dimensiune a cozii, se alege cel cu id-ul cel mai mic. Id-ul se află în funcție de poziția nodului în listă (deoarece sunt în ordine). O dată găsit nodul cu dimensiunea minimă a cozii se apelează metoda `addTask` a acestuia pentru a adăuga task-ul în coadă.

#### Size Interval Task Assignment
Deoarece această politică restricționează numărul de noduri la 3, singurul lucru ce trebuie verificat este tipul task-urilor (short, medium, long) și adăugarea lor în această ordine în coada task-urilor 0, 1, 2.

#### Least Work Left
Asemănător Shortest Queue, în cadrul acestei politici se compară timpul total rămas de executat. Iterez prin toate nodurile pentru a afla timpul minim rămas, iar dacă se găsește un minim îl rețin pentru adăugarea în coadă. Altfel, dacă două noduri au același timp rămas (calculat la granularitate de secunde folosind o constantă threshold de 1 secundă), se reține nodul cu id-ul cel mai mic. În final se adaugă task-ul în coada nodului cu cel mai mic timp rămas de executat.

Am folosit `synchronized` pentru a sincroniza accesul la metoda `addTask`.

## Implementarea Nodurilor

În clasa `MyHost` am implementat logica din spatele Nodurilor, principala metodă fiind metoda `run`. Pe lângă această metodă a fost necesară și implementarea unor metode adiacente folositoare în procesul de planificare și executare.

### Metoda run
Rulează atât timp cât nodul este pornit și realizează diferite operații asupra task-ului curent. Pentru un task cu durata de N secunde, nodul va petrece fix N secunde în metoda run prin apelul metodei `sleep`. Se actualizează timpul rămas de executat pentru task, iar dacă task-ul ajunge la finalul execuției, se apelează metoda `finish` și se scoate din coadă.

Blocul `synchronized (taskQueue)` se asigură că aceste operații de citire și scriere în `taskQueue` sunt realizate într-un mod sigur, evitându-se modificările concurente care pot afecta integritatea datelor sau care pot duce la conflicte.

### Metoda addTask
Adaugă un task valid în coadă; este sincronizată la fel ca cea din `MyDispatcher`.

### Metoda getQueueSize
Returnează dimensiunea cozii; analog ca `addTask` se folosește `synchronized` pentru a se asigura operațiile sigure asupra cozii.

### Metoda getWorkLeft
Iterează prin task-urile din coadă și returnează suma timpilor rămași de executat, deoarece task-urile rămân în coadă și pe durata execuției lor.

### Metoda shutdown
Modifică valoarea variabilei `exit` în true, marcând astfel sfârșitul execuției nodului curent.

### Metoda finish
Afișează un mesaj de finalizare a execuției task-ului curent.

Pentru a ține cont de caracteristicile task-urilor (prioritate și preemptabilitate) am folosit o coadă ce folosește un comparator. Comparatorul l-am implementat ținând cont de următoarele:
- dacă task-ul cu care se compară este cel care rulează și este și preemptibil, atunci se compară prioritățile celor două task-uri.
- dacă task-ul cu care se compară este oricare alt task din coadă în afară de primul (adică cel care rulează), atunci se compară după priorități, iar dacă au aceeași prioritate se compară după timpul de start.

Această implementare face posibilă ordonarea task-urilor în coadă în funcție de prioritate și preemptabilitate, așa cum se cere în enunț.
