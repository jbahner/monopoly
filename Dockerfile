FROM hseeberger/scala-sbt
ADD . /monopoly
WORKDIR /monopoly
CMD sbt run
