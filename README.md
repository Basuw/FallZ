# Fallz Backend

Cloner le repository

```bash
  git clone https://gitlab.isima.fr/fallz/source
```


```bash
  docker-compose -p fallz up --build 
```

## CI/CD

La CI/CD de ce projet se decompose en 3 stages :

- Scan du code a chaque merge request pour s'assurer de sa qualite
  - Scan statique : respect de la syntaxe, des normes
  - Scan logique : Potentielle null pointer exception, erreur d'injection spring
  - Scan des dependances depuis une base de donnees de l'owasp
    - Processus long donc execute seulement au merge sur master
- Execution des tests a chaque merge request
- Prepartion du jar pour deploiement au merge sur master

Si un des stages (scan ou test) echoue, la merge request ne peut pas fusionne sur la branche cible.
Cela permet de s'assurer de la bonne qualite du code present en production.