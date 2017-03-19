DATA_DIR=../data
BIN_DIR=../bin
SRC_DIR=../src

TEXT_DATA=~/exploratory-code/ML/word2vecstream.txt
VECTOR_DATA=$DATA_DIR/recipe-vector.bin

echo -----------------------------------------------------------------------------------------------------
echo -- Training vectors...
time $BIN_DIR/word2vec -train $TEXT_DATA -output $VECTOR_DATA -cbow 0 -size 200 -window 5 -negative 0 -hs 1 -sample 1e-3 -threads 12 -binary 1

echo -----------------------------------------------------------------------------------------------------
echo -- distance...

$BIN_DIR/distance $DATA_DIR/$VECTOR_DATA
