FROM google/cloud-sdk:alpine

# Provide Java for Firestore emulator
RUN apk add --update --no-cache openjdk11-jre

# Install firestore emulator
RUN gcloud components install cloud-firestore-emulator beta --quiet

# Allow connecting to Firestore emulator
EXPOSE 8080

# Start Firestore emulator
COPY firestore.sh .
RUN chmod +x firestore.sh
ENTRYPOINT ["./firestore.sh"]