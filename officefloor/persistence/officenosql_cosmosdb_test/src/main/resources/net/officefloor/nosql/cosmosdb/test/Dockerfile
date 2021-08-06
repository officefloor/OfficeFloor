FROM node:lts

# Allow connecting to emulator
EXPOSE 8080

# Ensure emulator dependencies are available
COPY package.json .
RUN npm install

# Provide fix
COPY fix.js .
RUN node fix.js

# Start emulator
COPY index.js .
ENTRYPOINT ["node", "index.js"]