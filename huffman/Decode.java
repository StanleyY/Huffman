package huffman;

import java.nio.file.*;
import java.io.*;
import java.util.*;

class Decode{

  static char[] decodeHeader(byte[] input){
    char[] tree = new char[(int)Math.pow((double)2, (double)input[2] + 1)];
    String[] storage = new String[input[2] + 1];
    Arrays.fill(storage, "");
    for(int i = 1; i < input[0] * 2 + 1; i = i + 2){
      char val = (char)input[i];
      int code_len = input[i + 1];
      storage[code_len] = storage[code_len] + val;
    }

    int code = 0;
    for (int i = storage.length - 1; i > 0; i--){
      String s = storage[i];
      if(s != ""){
        for(char c : s.toCharArray()){
          int index = 0;
          int temp = code;
          int mask = (1 << (i - 1));
          for(int j = 0; j < i; j++){
            if( (((temp & mask) >> (i - 1)) & 1) == 0){
              index = 2 * index + 1;
            }
            else{
              index = 2 * index + 2;
            }
            temp = temp << 1;
          }
          tree[index] = c;
          code += 1;
        }
      }
      code += 1;
      code = code >> 1;
    }
    return tree;
  }


  static char[] decodeString(byte[] input, char[] tree){
    int index = 2 * input[0] + 1;
    char[] output = new char[ (input.length - input[0]) * (input[2] - 1) ];
    int output_index = 0;
    int mask = (1 << 7);
    int tree_index = 0;
    while (index < input.length){
      byte b = input[index];
      for(int i = 0; i < 8; i++){
        if( (((b & mask) >> 7) & 1) == 0 ){
          tree_index = 2 * tree_index + 1;
        }
        else{
          tree_index = 2 * tree_index + 2;
        }
        if(tree_index > tree.length) break;
        if(tree[tree_index] != '\0'){
          output[output_index] = tree[tree_index];
          output_index++;
          tree_index = 0;
        }
        b = (byte)(b << 1);
      }
      index++;
    }
    return Arrays.copyOfRange(output, 0, output_index);
  }


  static byte[] read(String filename){
    try {
      FileInputStream input = new FileInputStream(filename);
      byte[] f = new byte[input.available()];
      input.read(f);
      input.close();
      return f;
    } catch (java.io.IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    return null;
  }


  static void write(String filename, char[] decodedString){
    try {
      PrintWriter output = new PrintWriter(filename);
      output.write(decodedString);
      output.flush();
      output.close();
      } catch (java.io.IOException e) {
        e.printStackTrace();
        System.exit(1);
      }
  }


  public static void main(String[] args){
    if (args.length < 2) {
      System.out.println("Usage: Decode [-c CANONICAL_TREE_FILE] SOURCEFILE TARGETFILE");
      System.exit(1);
    }
    byte[] input = read(args[0]);
    char[] tree = decodeHeader(input);
    char[] output = decodeString(input, tree);
    write(args[1], output);
  }
}