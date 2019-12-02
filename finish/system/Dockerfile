FROM open-liberty
COPY src/main/liberty/config /config/
ADD target/system.war /config/apps
RUN configure.sh
