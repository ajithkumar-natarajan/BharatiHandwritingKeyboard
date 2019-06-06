function [XVal, YVal] = plotTouchPoints(filePath)

fullFilepath = strcat(filePath, '/files/');

AllFiles = dir(fullFilepath);
FolderBool = [AllFiles.isdir];
subFolders = AllFiles(FolderBool);
for i = 3 : length(subFolders)
    dataPath = strcat(fullFilepath, subFolders(i).name);
    AllData = dir(dataPath);
    DataBool = not([AllData.isdir]);
    dataFiles = AllData(DataBool);
    
    for j = 1 : length(dataFiles)
        fullDataPath = strcat(dataPath + "/", dataFiles(j).name);
        delimiter = ',';

        %% Read columns of data as text:
        % For more information, see the TEXTSCAN documentation.
        formatSpec = '%s%s%[^\n\r]';

        %% Open the text file.
        fileID = fopen(fullDataPath,'r');

        %% Read columns of data according to the format.
        % This call is based on the structure of the file used to generate this
        % code. If an error occurs for a different file, try regenerating the code
        % from the Import Tool.
        dataArray = textscan(fileID, formatSpec, 'Delimiter', delimiter, 'TextType', 'string',  'ReturnOnError', false);
        dataArray = dataArray(1,1:2);
        %% Close the text file.
        fclose(fileID);

        %% Convert the contents of columns containing numeric text to numbers.
        % Replace non-numeric text with NaN.
        raw = repmat({''},length(dataArray{1}),length(dataArray)-1);
        for col=1:length(dataArray)-1
            raw(1:length(dataArray{col}),col) = mat2cell(dataArray{col}, ones(length(dataArray{col}), 1));
        end
        numericData = NaN(size(dataArray{1},1),size(dataArray,2));

        for col=[1,2]
            % Converts text in the input cell array to numbers. Replaced non-numeric
            % text with NaN.
            rawData = dataArray{col};
            for row=1:size(rawData, 1)
                % Create a regular expression to detect and remove non-numeric prefixes and
                % suffixes.
                regexstr = '(?<prefix>.*?)(?<numbers>([-]*(\d+[\,]*)+[\.]{0,1}\d*[eEdD]{0,1}[-+]*\d*[i]{0,1})|([-]*(\d+[\,]*)*[\.]{1,1}\d+[eEdD]{0,1}[-+]*\d*[i]{0,1}))(?<suffix>.*)';
                try
                    result = regexp(rawData(row), regexstr, 'names');
                    numbers = result.numbers;

                    % Detected commas in non-thousand locations.
                    invalidThousandsSeparator = false;
                    if numbers.contains(',')
                        thousandsRegExp = '^\d+?(\,\d{3})*\.{0,1}\d*$';
                        if isempty(regexp(numbers, thousandsRegExp, 'once'))
                            numbers = NaN;
                            invalidThousandsSeparator = true;
                        end
                    end
                    % Convert numeric text to numbers.
                    if ~invalidThousandsSeparator
                        numbers = textscan(char(strrep(numbers, ',', '')), '%f');
                        numericData(row, col) = numbers{1};
                        raw{row, col} = numbers{1};
                    end
                catch
                    raw{row, col} = rawData{row};
                end
            end
        end


        %% Create output variable
        TouchPoints = table;
        TouchPoints.X = cell2mat(raw(:, 1));
        TouchPoints.Y = cell2mat(raw(:, 2));

        %% Clear temporary variables
        clearvars fullDataPath delimiter formatSpec fileID dataArray ans raw col numericData rawData row regexstr result numbers invalidThousandsSeparator thousandsRegExp;

        %% Plot the character
        XVal = TouchPoints{:,{'X'}};
        YVal = TouchPoints{:,{'Y'}};
        figure;
        plot(XVal, YVal)
        title(subFolders(i).name +"  "+ dataFiles(j).name);
    end
end