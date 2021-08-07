FROM node:lts

# Use node working directory
WORKDIR /usr/src/app

# Copy in fils
COPY package.json .
COPY index.js .
COPY cosmos.sh .

# Ensure emulator dependencies are available
RUN npm install

# Allow connecting to emulator
EXPOSE 8080

# Start emulator
RUN chmod +x cosmos.sh
ENTRYPOINT ["./cosmos.sh"]