javac huffman/*.java

# Run against all samples provided.
for i in {0..9}
do
echo "Running Sample $i, Differences:"
echo ""
java huffman.Encode samples/text/sample$i.txt test.huf
diff test.huf samples/encoded/sample$i.huf
echo ""
done