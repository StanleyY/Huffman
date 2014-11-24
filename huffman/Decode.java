package huffman;

import java.nio.file.*;
import java.io.*;
import java.util.*;

/**
 * Decodes files that were encoded using Huffman Coding
 *
 * @author     Stanley Yang
 * @version    1.0
 * @since      2014-11-24
 */
class Decode{

  /**
   * Generates a binary tree from the header of a given binary file.
   * <p>
   * The second byte will always be EOF, therefore the third byte contains
   * the height of the tree minus one. The third byte is used to allocate
   * enough space for the binary tree.
   * <p>
   * I use a String[] to sort the characters and their codeword lengths.
   * The index in the array is their codeword length and I found that the
   * characters are in lexographical order. This removes the need for me to
   * do any real sorting and the same result is accomplished by appending to
   * the end of the String at that index. I would switch to a char[] if I
   * found that constant String concatenation was too slow.
   * 
   * @param input   The encoded binary file.
   * @return        A canonical Huffman binary tree.
   */
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

  /**
   * Generates the original text from the encoded binary file.
   * <p>
   * Reads the encoded binary file a byte at a time and then a bit at a
   * time until it reaches EOF. It uses the canonical Huffman tree generated
   * using decodeHeader().
   *
   * @param input   The encoded binary file
   * @return        A char[] that is the decoded string.
   */
  static char[] decodeString(byte[] input){
    int index = 2 * input[0] + 1;
    char[] output = new char[ (input.length - input[0]) * (input[2] - 1) ];
    int output_index = 0;
    int mask = (1 << 7);
    int tree_index = 0;

    char[] tree = decodeHeader(input);

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

  /**
   * Reads in a given filename and returns those bytes.
   *
   * @param filename  The filename or the directory and the filename.
   * @return          A byte[] containing the contents of the file.
   */
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

  /**
   * Writes decodedString to a given file.
   *
   * @param filename      The filename or directory and filename.
   * @param decodedString The char[] to be written.
   */
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

  /**
   *  Executes the program or prints out how to use it.
   */
  public static void main(String[] args){
    if (args.length < 2) {
      System.out.println("Usage: Decode [-c CANONICAL_TREE_FILE] SOURCEFILE TARGETFILE");
      System.exit(1);
    }
    byte[] input = read(args[0]);
    char[] output = decodeString(input);
    write(args[1], output);
  }
}