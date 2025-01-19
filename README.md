# AWS S3 Demo (Java 21 + Maven)

A production-oriented, Maven-based Java project demonstrating:

- **Listing** an S3 bucket (with pagination).
- **Retrieving** metadata for a specific file.
- **Downloading** that file to a specified folder, preserving the original filename.
- **Flexible credentials**: default chain or a custom `.properties` file.

## Features

1. **Java 21**: Uses the latest Java (21).
2. **Maven**: Simplifies builds and dependencies.
3. **AWS SDK for Java v2**: Interacts with S3.
4. **Default or Custom Credentials**:
   - DefaultCredentialsProvider (environment variables, `~/.aws/credentials`, IAM roles).
   - `MyPropertiesCredentialsProvider` for reading from a local `myapp.properties`.
5. **Pagination**: Lists **all** objects beyond the 1000-per-page limit.
6. **File Download**: Saves with the original filename into a specified directory.

## Getting Started

1. **Clone** this repository:
   ```bash
   git clone https://github.com/yourusername/aws-s3-demo.git
   cd aws-s3-demo
