import json
import os
import random
import time
from datetime import datetime
from kafka import KafkaProducer

kafka_address = os.getenv('KAFKA_ADDRESS') or 'localhost:9092'

producer = KafkaProducer(bootstrap_servers=[kafka_address])

while True:
    temperature_measurement = {
        "roomId": random.randint(1, 5),
        "thermometerId": random.randint(1,10),
        "temperature": random.uniform(18.0, 36.6),
        "timestamp": round(datetime.now().timestamp()*1000)
    }

    key = temperature_measurement['thermometerId'].to_bytes(8, byteorder='big')
    value = json.dumps(temperature_measurement).encode('ascii')
    producer.send('temperature-measurements', key=key, value=value)
    time.sleep(1/20000)

producer.close()
