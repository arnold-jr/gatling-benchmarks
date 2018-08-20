tweeter
---

Demo code to post tweets to the DC/OS example tweeter service using random
handles and contents.

# Bash

The script `run_test.sh` will accept system properties and
pass them to `sbt`. For example:

```bash
./runtest -Dtweeter.maxUsers=10
```

# Docker

The docker image will run the simulation.
