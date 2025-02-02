STRUKTURA PROJEKTU
==================

APLIKACJE
---------
1. [web-mvc-app](web-mvc-app)
2. [web-mvc-app-2](web-mvc-app-2)
3. [web-mvc-vt-app](web-mvc-vt-app)
4. [webflux-app](webflux-app)

W folderze każdej aplikacji znajduje się plik `Dockerfile`, za pomocą którego można zbudować obraz danej aplikacji.
Przed zbudowaniem obrazu należy skompilować aplikację poleceniem:

`mvn package`

Następnie zbudowany obraz zostanie wykorzystany w pliku `docker-compose`.

DODATKOWE PLIKI
---------------
- [docker-compose.yml](docker-compose.yml) – konfiguracja infrastruktury projektu
- [metrics.py](metrics.py) – skrypt do pobierania metryk z Prometheusa z folderu [results](results) po zakończeniu testów
- [run_tests.sh](run_tests.sh) – skrypt uruchamiający zdefiniowane scenariusze w skrypcie po kolei
- [stress-test.js](stress-test.js) – skrypt uruchamiający pojedynczy scenariusz

PRZYKŁADOWE WYWOŁANIE K6
------------------------
`K6_PROMETHEUS_RW_SERVER_URL=http://localhost:9090/api/v1/write \
k6 run -o experimental-prometheus-rw stress-test.js`
