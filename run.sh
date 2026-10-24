#!/usr/bin/env sh
# Запуск JavaFX-приложения «Студия звукозаписи» (Linux/macOS).
cd "$(dirname "$0")" && mvn -q javafx:run
