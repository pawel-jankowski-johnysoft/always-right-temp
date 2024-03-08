FROM python:3.11
RUN pip3 install kafka-python
COPY generator.py /app.py
ENTRYPOINT ["python3", "/app.py"]