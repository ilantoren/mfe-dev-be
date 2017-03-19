// Convert the word2vec bin output to json

#include <stdio.h>
#include <string.h>
#include <math.h>
#include <stdlib.h> // mac os x
#include <ctype.h>

const long long max_size = 2000;         // max length of strings
const long long max_w = 50;              // max length of vocabulary entries

int main(int argc, char **argv) {
  FILE *f, *fo;
  char file_name[max_size];
  char file_name_out[max_size];
  long long words, size, a, b;
  char ch;
  float *M;
  char *vocab;
  int flag;
  int c;
  int num_out;
  printf("Hello!!\n");
  if (argc < 3) {
    printf("Usage: ./bin2json <FILEIN> <FILEOUT\nwhere FILEIN contains word projections in the BINARY FORMAT and FILEOUT the output file.\n");
    return 0;
  }
  strcpy(file_name, argv[1]);
  strcpy(file_name_out, argv[2]);

  f = fopen(file_name, "rb");
  if (f == NULL) {
    printf("Input file not found\n");
    return -1;
  }
  fo = fopen(file_name_out, "wt");
  if (fo == NULL) {
    printf("Cannot open output file.");
    return -1;
  }

  fscanf(f, "%lld", &words);
  fscanf(f, "%lld", &size);
  vocab = (char *)malloc((long long)words * max_w * sizeof(char));
  M = (float *)malloc((long long)words * (long long)size * sizeof(float));
  if (M == NULL) {
    printf("Cannot allocate memory: %lld MB    %lld  %lld\n", (long long)words * size * sizeof(float) / 1048576, words, size);
    return -1;
  }
  fprintf(fo, "{\n");
  num_out = 0;
  for (b = 0; b < words; b++) {
    fscanf(f, "%s%c", &vocab[b * max_w], &ch);
    for (a = 0; a < size; a++) fread(&M[a + b * size], sizeof(float), 1, f);
    // Make sure no " in string
    if (strlen(&vocab[b*max_w]) < 3) continue;
    flag = 1;
    for (a = 0; a < strlen(&vocab[b * max_w]); a++) {
      c = vocab[b * max_w + a];
      if (!isalnum(c)) {
        flag = 0; break;
      }
    }
    if (flag == 0) continue;

    if (num_out > 0) fprintf (fo, ", \n");
    fprintf(fo, "\"%s\" : [", &(vocab[b * max_w]));
    for (a = 0; a < size; a++) fprintf(fo, "%f%c ", M[a + b * size], a < size - 1 ? ',' : ' ');
    fprintf(fo, "]");
    num_out++;
  }
  fprintf(fo, "\n}\n");
  fclose(fo);
  fclose(f);

  return 0;
}
