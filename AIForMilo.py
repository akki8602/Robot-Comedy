# AI backend for Milo
# Connecting and sending commands to Milo Java application

import socket
import openai
import wave
import sounddevice as sd
import scipy.io.wavfile as wavfile

BRIDGE_HOST = 'localhost'  # or the Java machine's IP
BRIDGE_PORT = 9999
openai.api_key = "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"

# FORMAT = pyaudio.paInt16
CHANNELS = 1
RATE = 16000
CHUNK = 1024
DURATION = 5
FILENAME = "user_recording.wav"

def record_audio():
    print("Recording for 5 seconds...")
    # audio = pyaudio.PyAudio()
    # stream = audio.open(format=FORMAT, channels=CHANNELS,
    #                     rate=RATE, input=True, frames_per_buffer=CHUNK)

    # frames = []
    # for _ in range(0, int(RATE / CHUNK * RECORD_SECONDS)):
    #     frames.append(stream.read(CHUNK))

    # stream.stop_stream()
    # stream.close()
    # audio.terminate()

    # with wave.open(WAVE_OUTPUT_FILENAME, 'wb') as wf:
    #     wf.setnchannels(CHANNELS)
    #     wf.setsampwidth(audio.get_sample_size(FORMAT))
    #     wf.setframerate(RATE)
    #     wf.writeframes(b''.join(frames))
    audio = sd.rec(int(DURATION * RATE), samplerate=RATE, channels=CHANNELS, dtype='int16')
    sd.wait()
    wavfile.write(FILENAME, RATE, audio)
    print("Done recording.\n")

def transcribe_audio():
    print("Transcribing...")
    with open("user_recording.wav", "rb") as audio_file:
        transcript = openai.audio.transcriptions.create(
            model="whisper-1",
            language="en",
            file=audio_file
        )
        print(transcript.text)
        return transcript.text

def send_to_LLM(user_input):
    response = openai.chat.completions.create(
        model="gpt-4",
        messages=[
            {"role": "system", "content": "You are a funny robot named Milo. Make jokes on the user as they talk"},
            {"role": "user", "content": user_input}
        ]
    )
    print(response.choices[0].message.content)
    return response.choices[0].message.content

def send_to_milo(message):
    try:
        with socket.create_connection((BRIDGE_HOST, BRIDGE_PORT)) as sock:
            print("Connected to Java bridge.");
                # command = input("Enter command to Milo (or 'exit'): ")
                # if command.strip().lower() == "exit":
                #     break
            sock.sendall((message + "\n").encode('utf-8'))
    except Exception as e:
        print("Socket error:", e)

def main():
    print("Type 'record' to speak. Type 'quit' to exit.\n")
    while True:
        cmd = input(">>> ").strip().lower()
        if cmd == "record":
            record_audio()
            text = transcribe_audio()
            response = send_to_LLM(text)
            send_to_milo(response)
        elif cmd == "quit":
            print("Exiting.")
            break
        else:
            print("Unknown command. Try 'record' or 'quit'.")

if __name__ == "__main__":
    main()