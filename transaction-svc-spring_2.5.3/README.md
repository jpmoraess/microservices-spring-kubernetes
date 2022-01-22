## Serviço persistência de transações para posterior liquidação por produto de domínio específico, valida as transações e persiste no DynamoDB e manda notificações 
## no caso de suspeita de fraudes.

Execução

1) gradle clean build
2) Subir localmente kafka, zipkin e redis
    docker-compose -f docker-compose-kafka-zipkin.yml up
3) gradle bootRun

Alterar as credenciais da AWS para o dynamoDB no arquivo application.yaml

4) amazon:
     aws:
      accesskey: XXXXXXXXXXXXX
      secretkey: YYYYYYYYYYYYYYYYYY

![Arquitetura alta](documents/CoffeeAndIT.png)
