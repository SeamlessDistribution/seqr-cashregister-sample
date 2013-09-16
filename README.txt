This is a bare-bone implementation for a cashregister that supports SEQR payments.

To run the sample, you need JRE6 and maven installed. You also need to
replace the marked places in Main.java with the credentials you have
received from SEQR support.
Then running the following command from command-line should run the sample:

mvn exec:java -Dexec.mainClass="com.seamless.ers.common.provisioner.sample.Main"
