# Resume Classifier Project

## Description
This Maven-based Java project reads PDF resumes from `C:\Resumes`, extracts name, college, role, and skill classification based on keywords, and outputs a CSV file at `C:\Resumes\output\ResumeData.csv`.

## How to Run
1. Place your PDF resumes in `C:\Resumes`.
2. In the project directory, run:
   ```bash
   mvn clean compile exec:java -Dexec.mainClass="com.resume.parser.ResumeClassifier"
   ```
3. The output CSV will be generated at `C:\Resumes\output\ResumeData.csv`.
