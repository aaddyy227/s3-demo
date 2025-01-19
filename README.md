
# AWS S3 Demo (Java 21 + Maven + Pagination)

A **Maven-based Java 21** application demonstrating interaction with **AWS S3**, including:

- **Listing** an S3 bucket with **pagination** (beyond 1000 items per page).
- **Retrieving** metadata for a specific object key.
- **Downloading** that object to a **specified local directory**, preserving the original filename.
- **Flexible credentials management**: using **DefaultCredentialsProvider** or a **custom `.properties` file**.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Setup](#setup)
  - [1. Clone the Repository](#1-clone-the-repository)
  - [2. Configure AWS Credentials](#2-configure-aws-credentials)
  - [3. Configure Application Properties](#3-configure-application-properties)
- [Building the Project](#building-the-project)
- [Running the Application](#running-the-application)
  - [Option A: Using Default Credentials](#option-a-using-default-credentials)
  - [Option B: Using File-Based Credentials](#option-b-using-file-based-credentials)
- [Troubleshooting](#troubleshooting)
- [Security Best Practices](#security-best-practices)
- [Contributing](#contributing)
- [License](#license)

## Prerequisites

- **Java 21** installed on your machine.
- **Maven** (3.x+) installed.
- An **AWS account** with access to S3 and appropriate permissions.
- **Git** installed (optional, for cloning the repository).

## Project Structure

```
aws-s3-demo/
├── .gitignore
├── README.md
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── example/
        │           ├── S3App/
        │           │   ├── S3DemoApp.java
        │           │   ├── S3ResourceServiceInterface.java
        │           │   ├── S3ResourceService.java
        │           │   ├── Resource.java
        │           │   └── ListResult.java
        │           └── credentials/
        │               └── MyPropertiesCredentialsProvider.java
        └── resources/
            ├── application.properties (if using file-based credentials)
```

### Description of Key Files

- **`S3DemoApp.java`**: The main class that initializes the application, handles configuration, and demonstrates S3 operations.
- **`S3ResourceServiceInterface.java`**: Interface defining methods for interacting with S3 resources.
- **`S3ResourceService.java`**: Concrete implementation of the interface, handling S3 operations like listing, retrieving, and downloading objects.
- **`Resource.java`**: Model class representing an S3 resource (file or folder).
- **`ListResult.java`**: Wrapper class for a list of resources along with a pagination cursor.
- **`MyPropertiesCredentialsProvider.java`**: Custom credentials provider that loads AWS credentials from a `.properties` file.
- **`application.properties`**: Configuration file for AWS region, bucket name, object key, and download path.
- **`myapp.properties`**: (Optional) Configuration file for AWS access key and secret key if using file-based credentials.

## Setup

### 1. Clone the Repository

Clone this repository to your local machine using Git:

```bash
git clone https://github.com/aaddyy227/s3-demo.git
cd aws-s3-demo
```

### 2. Configure AWS Credentials

You have two options to provide AWS credentials:

#### Option A: Using Default Credentials Provider

The AWS SDK for Java uses the **DefaultCredentialsProvider** which looks for credentials in the following order:

1. **Environment Variables**:
   - `AWS_ACCESS_KEY_ID`
   - `AWS_SECRET_ACCESS_KEY`

2. **Java System Properties**:
   - `aws.accessKeyId`
   - `aws.secretAccessKey`

3. **Default Credentials File** (`~/.aws/credentials`):
   ```ini
   [default]
   aws_access_key_id = YOUR_ACCESS_KEY
   aws_secret_access_key = YOUR_SECRET_KEY
   ```

4. **AWS Instance Profile Credentials** (if running on AWS services like EC2).

**Set Environment Variables Example (Windows CMD):**

```bat
set AWS_ACCESS_KEY_ID=YOUR_ACCESS_KEY
set AWS_SECRET_ACCESS_KEY=YOUR_SECRET_KEY
```

**Set Environment Variables Example (Unix/Linux/Mac):**

```bash
export AWS_ACCESS_KEY_ID=YOUR_ACCESS_KEY
export AWS_SECRET_ACCESS_KEY=YOUR_SECRET_KEY
```

#### Option B: Using File-Based Credentials

Alternatively, you can store your AWS credentials in a `myapp.properties` file.

1. **Create `myapp.properties`** in `src/main/resources/` with the following content:

   ```properties
   aws.accessKey=YOUR_ACCESS_KEY
   aws.secretKey=YOUR_SECRET_KEY
   ```

2. **Ensure `myapp.properties` is **not** committed to version control** by verifying it's listed in `.gitignore`.

### 3. Configure Application Properties

Edit `src/main/resources/application.properties` to specify your AWS configuration:

```properties
# AWS Configuration
aws.s3.region=us-east-1
aws.s3.bucketName=my-qteam-bucket
aws.s3.objectKey=test-folder/hello-world.txt
aws.s3.downloadPath=C:/Users/aaddy/Downloads
```

- **`aws.s3.region`**: AWS region (e.g., `us-east-1`).
- **`aws.s3.bucketName`**: Name of your S3 bucket.
- **`aws.s3.objectKey`**: The S3 key you want to retrieve/download.
- **`aws.s3.downloadPath`**: Local directory path where downloaded files will be saved. Ensure this directory exists or the application has permission to create it.

## Building the Project

Ensure you are in the project root directory (where `pom.xml` is located) and run:

```bash
mvn clean compile
```

This command will compile the Java source files and download necessary dependencies.

## Running the Application

You can run the application in two modes:

### Option A: Using Default Credentials

If you have set up AWS credentials via environment variables or the default credentials file, run the application without additional arguments:

```bash
mvn exec:java -Dexec.mainClass="com.example.S3App.S3DemoApp"
```

**Example (Windows CMD):**

```bat
mvn exec:java -Dexec.mainClass="com.example.S3App.S3DemoApp"
```

### Option B: Using File-Based Credentials

1. **Create `myapp.properties`** in `src/main/resources/` with your AWS credentials:

   ```properties
   aws.accessKey=YOUR_ACCESS_KEY
   aws.secretKey=YOUR_SECRET_KEY
   ```

2. **Run the Application with the `file` Argument:**

   ```bash
   mvn exec:java -Dexec.mainClass="com.example.S3App.S3DemoApp" -Dexec.args="file"
   ```

**Example (Windows CMD):**

```bat
mvn exec:java -Dexec.mainClass="com.example.S3App.S3DemoApp" -Dexec.args="file"
```

## Troubleshooting

- **Credentials Not Found:**...
