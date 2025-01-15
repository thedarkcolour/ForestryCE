import os

def reorganize_filenames():
    # Get the directory where the script is located
    script_directory = os.path.dirname(os.path.abspath(__file__))

    # Loop through all files in the directory
    for filename in os.listdir(script_directory):
        filepath = os.path.join(script_directory, filename)

        # Skip directories, only process files
        if not os.path.isfile(filepath):
            continue

        # Check if 'stripped_' exists in the filename
        if 'stripped_' in filename:
            # Remove 'stripped_' and place it at the beginning
            new_filename = 'stripped_' + filename.replace('stripped_', '', 1)

            # Get the full path of the new filename
            new_filepath = os.path.join(script_directory, new_filename)

            # Rename the file and delete the original
            os.rename(filepath, new_filepath)

if __name__ == "__main__":
    reorganize_filenames()
    print("Filenames reorganized successfully!")