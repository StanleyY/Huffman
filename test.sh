javac huffman/Decode.java
# Run against all samples provided.
for i in {0..9}
do
  echo "Running Sample $i, Differences:"
  java huffman.Decode samples/encoded/sample$i.huf test.txt
  diff test.txt samples/text/sample$i.txt
  echo ""
done