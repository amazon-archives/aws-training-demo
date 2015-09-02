#Parallel vs Sequential Uploads

**Purpose**: Demonstrate that S3 is massively parallel.   
**Summary**: This demonstration uploads 500 x 10MB files (5GB total). When done sequentially, it takes 18 minutes. When done in parallel, it takes 70 seconds.

Do not confuse parallel uploads with [multipart uploads](http://docs.aws.amazon.com/AmazonS3/latest/dev/mpuoverview.html)

##Step 1: Create a bucket for storing files
The Region for the bucket should match the Region used for EC2 in the next step. I also add a Lifecycle rule to expire files after 0 days, just to keep things clean.

```
export BUCKET=<your unique bucket name>
aws s3 mb s3://$BUCKET
```

##Step 2: Launch a Linux m2.4XL instance with ephemeral storage in the same Region
- Use ``m2.4XL`` to avoid network bandwidth problems
- Give it a Role with permissions to access your S3 Bucket
- Add one default Instance Store volume to store the 5GB of data. It also makes a great demonstration of ephemeral storage.
- Make sure the instance is in the same Region as the Bucket!

```
Should we add a AWS CLI script for these steps (Role, SG, KP, Instance) ?
Or a cloudformation template ?
```

##Step 3: Login to EC2 and setup environment
I paste these commands into the Command Line. It downloads GNU Parallel and flicks to the ephemeral storage.

```
# Download Gnu Parallel
wget http://ftp.gnu.org/gnu/parallel/parallel-20101202.tar.bz2
tar -jxvf parallel-20101202.tar.bz2
cd parallel-20101202
./configure
make && sudo make install

# Go to instance store
cd /media/ephemeral0/
sudo chmod 777 .
```
##Step 4: Create 500 x 10MB files
```
# Create files
for num in $(seq 1 500); do dd if=/dev/zero of=file-${num} bs=1M count=10; done
```

##Step 5: Sequential upload demonstration

```
# Sequential upload
time for ob in *; do echo ${ob}; aws s3 $ob s3://$BUCKET/seq-$ob; done
````

This should take approximately 18 minutes. Go back to teaching the course and come back later to see the result. It will look something like this:

```
real	18m20.396s
user	7m25.308s
sys		0m44.327s
```

##Step 6: Parallel upload demonstration

```
# Parallel upload
time ls * | parallel -j500 aws s3 $ob s3://$BUCKET/par-$ob
```
The result will look like:
```
real	1m12.777s
user	7m59.842s
sys	  1m4.388s
```

**Bottom line**: Parallel uploads on S3 are much faster than sequential uploads

## Cleanup

Delete the bucket 

```
aws s3 rb --force s3://$BUCKET
```

## Credits

Dean Samuels for the script   
John Rotenstein for the wiki page   
Sebastien Stormacq to adapt to AWS CLI 1.0
