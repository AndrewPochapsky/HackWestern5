from tensorflow.keras.preprocessing.image import ImageDataGenerator
import tensorflow as tf
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Convolution2D
from tensorflow.keras.layers import MaxPooling2D
from tensorflow.keras.layers import Flatten
from tensorflow.keras.layers import Dense

model = Sequential()

image_size = 64

#Convolution
model.add(Convolution2D(32, 3, 3, input_shape=(image_size, image_size, 3), activation='relu'))

#Pooling
model.add(MaxPooling2D(pool_size = (2,2)))

#Flatten layer into a 1D vector
model.add(Flatten())

model.add(Dense(128, activation= 'relu'))
model.add(Dense(17, activation='softmax'))

#Compiling
model.compile(optimizer=tf.keras.optimizers.Adam(),
              loss='categorical_crossentropy',
              metrics=['accuracy'])

#Fitting

train_datagen = ImageDataGenerator(rescale=1./255,
                                   shear_range=0.2,
                                   zoom_range=0.2,
                                   horizontal_flip=True)

test_datagen = ImageDataGenerator(rescale=1./255)

training_set = train_datagen.flow_from_directory('data/training',
                                                 target_size=(image_size, image_size),
                                                 batch_size=32,
                                                 class_mode='categorical')

test_set = test_datagen.flow_from_directory('data/testing',
                                            target_size=(image_size, image_size),
                                            batch_size=32,
                                            class_mode='categorical')

model.fit_generator(training_set,
                    steps_per_epoch=1088,
                    epochs=1,
                    validation_data=test_set,
                    validation_steps=272,
                    use_multiprocessing=True, workers=8)

# Save tf.keras model in HDF5 format.
keras_file = "keras_model.h5"
tf.keras.models.save_model(model, 'models/'+keras_file)

# Convert to TensorFlow Lite model.
