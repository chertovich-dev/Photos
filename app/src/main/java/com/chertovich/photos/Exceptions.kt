package com.chertovich.photos

open class PhotosException : Exception()
class UnknownException : PhotosException()
class LoginIsEmptyException : PhotosException()
class PasswordIsEmptyException : PhotosException()
class WrongSizeOfLoginException : PhotosException()
class WrongSizeOfPasswordException : PhotosException()
class PasswordIsIncorrectException : PhotosException()
class LoginAlreadyUsedException : PhotosException()
class ImageIsTooBigException : PhotosException()
class LoadingPhotoException : PhotosException()