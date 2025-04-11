echo "[BUILD] Compiling Java files..."
mkdir -p bin
javac -d bin -sourcepath src src/app/App.java