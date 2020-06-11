FROM hseeberger/scala-sbt:8u171_2.12.6_1.1.6
RUN mkdir -p /mainModule
ADD . /mainModule
WORKDIR /mainModule
CMD sbt run