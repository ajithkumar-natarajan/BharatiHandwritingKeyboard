import os
import sys
from datetime import datetime

walk_dir = sys.argv[1]

#print('walk_dir = ' + walk_dir)

# If your current working directory may change during script execution, it's recommended to
# immediately convert program arguments to an absolute path. Then the variable root below will
# be an absolute path as well. Example:
# walk_dir = os.path.abspath(walk_dir)

#print('walk_dir (absolute) = ' + os.path.abspath(walk_dir))

for root, subdirs, files in os.walk(walk_dir):
    # print('--\nroot = ' + root)
    formatter_data_path = os.path.join(root, 'data.ndjson')
    # print('formatter_data_path = ' + formatter_data_path)

    with open(formatter_data_path, 'wb') as formatter_data:
        for subdir in subdirs:
            print('\t- subdirectory ' + subdir)

        for filename in files:
            file_path = os.path.join(root, filename)

            print('\t- file %s (full path: %s)' % (filename, file_path))

            time = datetime.utcnow();
            timeStr = time.strftime('%Y-%m-%d %H:%M:%S.%f')
            timestamp = timeStr[:-1]
            wordName = root[root.rfind("/")+1:]
            with open(os.path.join(walk_dir, "Username.txt")) as usernameFile:
                username = usernameFile.read()
            formatter_data.write(('{"word":"%s","username":"%s","timestamp":"%s UTC","recognized":true,"key_id":"1234567890987654","drawing":[' % (wordName, username, timestamp)).encode('utf-8'))

            flag = False
            n = 0
            with open(file_path, 'rt') as f:
                f_content = f.readlines()
                # print(f_content)
                length = len(f_content)
                # for i in range (n,length):
                i = n
                # print (length)
                while(i<length-1):
                    if not(n==0):
                        print(',', end='')
                    print('[[', end='')
                    for j in range (i, length):
                        f_content[j] = f_content[j].rstrip("\n\r")
                        subString = f_content[j][f_content[j].find("q")+1:f_content[j].find(",")]
                        if not(subString.find("l")):
                            subString = subString[1:]+']'
                            print(subString)
                            n = j
                            break
                        print(subString)
                        
                    print('--')
                    print(',[', end='')

                    for j in range (i, length):
                        f_content[j] = f_content[j].rstrip("\n\r")
                        if not(f_content[j].find("l")):
                            flag = True    
                        subString = f_content[j][f_content[j].find(",")+1:f_content[j].find(";")]
                        if(flag):
                            subString = subString+']]'
                            flag = False
                            print(subString)
                            n = j
                            break
                        print(subString)

                    print('--')

                    # print('i=%d n=%d length=%d'%(i,n, length))
                    i = n+1
                print(']}')
                    
                    # print f_content[i]
                # formatter_data.write(('The file %s contains:\n' % filename).encode('utf-8'))
                # formatter_data.write(f_content)
                # formatter_data.write(b'\n')

"""
Printing data in format required to write to the ndjson file

import os
import sys
from datetime import datetime

walk_dir = sys.argv[1]

#print('walk_dir = ' + walk_dir)

# If your current working directory may change during script execution, it's recommended to
# immediately convert program arguments to an absolute path. Then the variable root below will
# be an absolute path as well. Example:
# walk_dir = os.path.abspath(walk_dir)

#print('walk_dir (absolute) = ' + os.path.abspath(walk_dir))

for root, subdirs, files in os.walk(walk_dir):
    # print('--\nroot = ' + root)
    formatter_data_path = os.path.join(root, 'data.ndjson')
    # print('formatter_data_path = ' + formatter_data_path)

    with open(formatter_data_path, 'wb') as formatter_data:
        for subdir in subdirs:
            print('\t- subdirectory ' + subdir)

        for filename in files:
            file_path = os.path.join(root, filename)

            print('\t- file %s (full path: %s)' % (filename, file_path))

            time = datetime.utcnow();
            timeStr = time.strftime('%Y-%m-%d %H:%M:%S.%f')
            timestamp = timeStr[:-1]
            wordName = root[root.rfind("/")+1:]
            with open(os.path.join(walk_dir, "Username.txt")) as usernameFile:
                username = usernameFile.read()
            formatter_data.write(('{"word":"%s","username":"%s","timestamp":"%s UTC","recognized":true,"key_id":"1234567890987654","drawing":[' % (wordName, username, timestamp)).encode('utf-8'))

            flag = False
            n = 0
            with open(file_path, 'rt') as f:
                f_content = f.readlines()
                # print(f_content)
                length = len(f_content)
                # for i in range (n,length):
                i = n
                # print (length)
                while(i<length-1):
                    if not(n==0):
                        print(',', end='')
                    print('[[', end='')
                    for j in range (i, length):
                        f_content[j] = f_content[j].rstrip("\n\r")
                        subString = f_content[j][f_content[j].find("q")+1:f_content[j].find(",")]
                        if not(subString.find("l")):
                            subString = subString[1:]+']'
                            print(subString)
                            n = j
                            break
                        print(subString)
                        
                    print('--')
                    print(',[', end='')

                    for j in range (i, length):
                        f_content[j] = f_content[j].rstrip("\n\r")
                        if not(f_content[j].find("l")):
                            flag = True    
                        subString = f_content[j][f_content[j].find(",")+1:f_content[j].find(";")]
                        if(flag):
                            subString = subString+']]'
                            flag = False
                            print(subString)
                            n = j
                            break
                        print(subString)

                    print('--')

                    # print('i=%d n=%d length=%d'%(i,n, length))
                    i = n+1
                print(']}')
                    
                    # print f_content[i]
                # formatter_data.write(('The file %s contains:\n' % filename).encode('utf-8'))
                # formatter_data.write(f_content)
                # formatter_data.write(b'\n')
"""