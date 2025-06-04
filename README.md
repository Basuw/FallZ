# Fallz Backend

University Porject done for SAÉ - IoT : PoC & PoT in 2nd year of Engineer degree at [ISIMA](https://www.isima.fr/)

## Description

Fall detector for hikers 🚶🏼‍♂️. Embedded systems which detect the fall using a accelerometer and a gyroscope. These Data are handled by a machine learning algorithm to detect if it is a fall or not exported on the arduino.
It uses Lorawan protocol to transmit the position of the user, obtained with a GPS module 🌍. These Data are transmitted via TheThingsStack broker and retrieved by our backend. Possible to create an account with multiple devices, each of them related to one person. You can track all your hikes on a map but also you falls and these data are storde in a PostgreSQL database 💾.

## User notice

Clone repository

```bash
  git clone https://gitlab.isima.fr/fallz/source
```

```bash
  docker-compose -p fallz up --build 
```

## CI/CD

The CI/CD of this project is divided into three stages:

- Code scanning at each merge request to ensure its quality
- Static scanning: compliance with syntax and standards
- Logical scanning: potential null pointer exceptions, Spring injection errors
- Dependency scanning from an OASPS database
- A lengthy process, therefore executed only at the merge on master
- Execution of tests at each merge request
- Preparation of the JAR for deployment at the merge on master

If one of the stages (scan or test) fails, the merge request cannot be merged into the target branch.
This ensures the high quality of the code in production.

## Authors

- [Anderson BURENGERO-SHEMA](https://gitlab.isima.fr/anburenger)
- [Augustin QUATREFAGES](https://gitlab.isima.fr/auquatrefa)
- [Léo RUY](https://gitlab.isima.fr/leruy)
- [Yoan BOYER](https://gitlab.isima.fr/yoboyer)
- [Bastien JACQUELIN](https://github.com/Basuw)
