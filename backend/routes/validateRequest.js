const validateRequest = (requiredFields = []) => {
  return (req, res, next) => {
    const missingFields = [];

    for (const field of requiredFields) {
      const value = req.body?.[field];

      if (
        value === undefined ||
        value === null ||
        String(value).trim() === ''
      ) {
        missingFields.push(field);
      }
    }

    if (missingFields.length > 0) {
      return res.status(400).json({
        success: false,
        message: 'Required fields are missing',
        missingFields,
      });
    }

    next();
  };
};

export default validateRequest;
